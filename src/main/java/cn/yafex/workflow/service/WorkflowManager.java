package cn.yafex.workflow.service;

import cn.yafex.workflow.model.*;
import cn.yafex.workflow.execution.WorkflowContext;
import cn.yafex.workflow.execution.WorkflowStatus;
import cn.yafex.workflow.util.WorkflowLogger;
import cn.yafex.workflow.util.JsonFileHandler;
import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.core.ToolRegistry;
import cn.yafex.tools.exceptions.ToolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.alibaba.fastjson.JSON;
import java.util.List;

/**
 * 条件操作符枚举
 */
enum ConditionOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN_OR_EQUALS
}

/**
 * Service for managing and executing workflows
 */
@Service
public class WorkflowManager {
    private final JsonFileHandler jsonFileHandler;
    private final WorkflowLogger workflowLogger;
    private final ExecutorService executorService;
    private final Map<String, WorkflowContext> activeWorkflows;
    private final WorkflowDebugService debugService;
	
    @Autowired
    public WorkflowManager(JsonFileHandler jsonFileHandler, WorkflowLogger workflowLogger, WorkflowDebugService debugService) {
        this.jsonFileHandler = jsonFileHandler;
        this.workflowLogger = workflowLogger;
        this.debugService = debugService;
        this.executorService = Executors.newCachedThreadPool();
        this.activeWorkflows = new ConcurrentHashMap<>();
    }

    /**
     * 启动工作流执行
     * @param workflowId 要执行的工作流ID
     * @param initialVariables 初始全局变量
     * @return 执行ID
     */
    public String startWorkflow(String workflowId, Map<String, Object> initialVariables) {
        try {
			System.out.println("startWorkflow: " + workflowId);
            Workflow workflow = jsonFileHandler.loadWorkflow(workflowId);
            WorkflowContext context = new WorkflowContext(workflowId);
            
            if (initialVariables != null) {
                context.setVariables(initialVariables);
            }
            
            activeWorkflows.put(context.getExecutionId(), context);
            workflowLogger.logWorkflowStart(context.getExecutionId(), workflowId);

            // Start workflow execution in a separate thread
            executorService.submit(() -> executeWorkflow(workflow, context));

            return context.getExecutionId();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start workflow: " + workflowId, e);
        }
    }

    /**
     * 执行工作流
     * @param workflow 要执行的工作流
     * @param context 执行上下文
     */
    private void executeWorkflow(Workflow workflow, WorkflowContext context) {
        long startTime = System.currentTimeMillis();
        String currentNodeId = workflow.getStartNodeId();

        try {
            // Initialize workflow inputs
            context.setVariables(new HashMap<>(workflow.getInputs()));

            while (currentNodeId != null) {
                WorkflowNode node = workflow.getNodeById(currentNodeId);
                if (node == null) {
                    throw new RuntimeException("Node not found: " + currentNodeId);
                }

                context.setCurrentNodeId(currentNodeId);
                long nodeStartTime = System.currentTimeMillis();
                
                // 收集节点执行参数信息
                Map<String, Object> nodeParameters = new HashMap<>();
                nodeParameters.put("toolName", node.getToolName());
                if (node.getType() == NodeType.FUNCTION && workflow.hasTool(node.getToolName())) {
                    ToolDefinition toolDef = workflow.getToolDefinition(node.getToolName());
                    nodeParameters.put("toolDescription", toolDef.getDescription());
                }

                // 设置节点进入事件
                NodeExecutionEvent enterEvent = new NodeExecutionEvent(
                    context.getExecutionId(),
                    node.getId(),
                    node.getName(),
                    node.getType().toString(),
                    "ENTER"
                );
                enterEvent.setContextVariables(new HashMap<>(context.getVariables()));
                debugService.sendDebugEvent(enterEvent);
                
                // 根据节点类型执行节点
                Map<String, Object> nodeResult = executeNode(node, context);
                
                // 更新节点上下文
                // if (node.getType() == NodeType.FUNCTION) {
                //     node.setContext(new HashMap<>(nodeResult));
                // }

                long nodeDuration = System.currentTimeMillis() - nodeStartTime;
                workflowLogger.logNodeExecution(
                    context.getExecutionId(),
                    workflow.getId(),
                    node.getName(),
                    node.getType().toString(),
                    nodeParameters,
                    nodeResult,
                    nodeDuration
                );

                // 发送节点完成事件
                NodeExecutionEvent completeEvent = new NodeExecutionEvent(
                    context.getExecutionId(),
                    node.getName(),
                    node.getName(),
                    node.getType().toString(),
                    "COMPLETE"
                );
                completeEvent.setNodeResult(nodeResult);
                completeEvent.setDuration(nodeDuration);
                debugService.sendDebugEvent(completeEvent);

                // 确定下一个节点
                if (node.getType() == NodeType.END) {
                    // 将相关上下文变量复制到工作流输出
                    workflow.getOutputs().putAll(context.getVariables());
                    currentNodeId = null;
                } else if (node.getType() == NodeType.CONDITION) {
                    // 从节点执行结果中获取条件评估结果
                    Map<String, Object> evaluationResult = nodeResult;
                    String matchedCase = (String) evaluationResult.get("matchedCase");
                    currentNodeId = node.getNextNodes().get(matchedCase);
                } else {
                    currentNodeId = node.getNextNodes().get("default");
                }
            }
            
            context.setStatus(WorkflowStatus.COMPLETED);
        } catch (Exception e) {
            context.setStatus(WorkflowStatus.FAILED);
            e.printStackTrace();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            workflowLogger.logWorkflowComplete(
                context.getExecutionId(),
                workflow.getName(),
                context.getStatus().toString(),
                duration
            );
            debugService.completeDebugSession(context.getExecutionId());
            activeWorkflows.remove(context.getExecutionId());
        }
    }

    /**
     * 执行节点
     * @param node 要执行的节点
     * @param context 执行上下文
     * @return 节点执行结果
     */
    private Map<String, Object> executeNode(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (node.getType()) {
                case START:
                    // 开始节点初始化全局变量
                    result.putAll(context.getVariables());
                    break;
                    
                case FUNCTION:
                    if (node.getToolName() != null) {
                        // 准备工具输入
                        Map<String, Object> toolInputs = prepareToolInputs(node, context);
                        
                        // 执行工具并获取其输出
                        result = executeTool(node.getToolName(), toolInputs);
                        // 更新上下文
                        context.getVariables().putAll(result);
                    }
                    break;
                    
                case CONDITION:
                    // 条件节点不修改上下文
                    result.putAll(evaluateCondition(node, context));
                    break;
                    
                case END:
                    // 结束节点捕获最终状态
                    result.putAll(context.getVariables());
                    break;
            }
        } catch (ToolException e) {
            result.put("error", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("details", e.getDetails());
        }
        
        return result;
    }

    /**
     * Prepare tool inputs based on inputMap if available
     * @param node Function node with inputMap
     * @param context Execution context
     * @return Prepared inputs for the tool
     */
    private Map<String, Object> prepareToolInputs(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> toolInputs = new HashMap<>(context.getVariables());
        
        // If inputMap is not defined or empty, use all context variables as inputs
        if (node.getInputMap() == null || node.getInputMap().isEmpty()) {
            return toolInputs;
        }
        
        // Create a new map for tool inputs
        Map<String, Object> mappedInputs = new HashMap<>();
        
        // Process each input mapping
        for (Map.Entry<String, VariableDefinition> entry : node.getInputMap().entrySet()) {
            String paramName = entry.getKey();
            VariableDefinition varDef = entry.getValue();
            
            if (varDef == null) {
                continue;
            }
            
            if ("CONSTANT".equals(varDef.getName())) {
                // For constants, use the value directly
                mappedInputs.put(paramName, varDef.getValue());
            } else {
                // For variables, look up the value in context
                Object value = null;
                
                if (varDef.getParent() != null && !varDef.getParent().isEmpty()) {
                    // If parent is specified, look up the value in the parent context
                    // Parent could be a node ID or another context identifier
                    Map<String, Object> parentContext = getParentContext(varDef.getParent(), context);
                    if (parentContext != null && parentContext.containsKey(varDef.getName())) {
                        value = parentContext.get(varDef.getName());
                    }
                } else {
                    // No parent specified, look in global context
                    value = context.getVariables().get(varDef.getName());
                }
                
                if (value != null) {
                    mappedInputs.put(paramName, value);
                }
            }
        }
        
        return mappedInputs;
    }
    
    /**
     * Get parent context based on parent identifier
     * @param parentId Parent identifier (usually a node ID)
     * @param context Current workflow context
     * @return Parent context map or null if not found
     */
    private Map<String, Object> getParentContext(String parentId, WorkflowContext context) {
        // If parent is a node ID, get the node's context
        if (context.getWorkflow() != null) {
            for (WorkflowNode node : context.getWorkflow().getNodes()) {
                if (parentId.equals(node.getId())) {
                    return node.getContext();
                }
            }
        }
        
        // If parent is not found or not a node ID, return global context
        return context.getVariables();
    }

    /**
     * Execute a tool with given inputs
     * @param toolName Name of the tool to execute
     * @param inputs Tool input parameters
     * @return Tool execution results
     */
    private Map<String, Object> executeTool(String toolName, Map<String, Object> inputs) throws ToolException {
        ToolHandler handler = ToolRegistry.getHandler(toolName);
        if (handler == null) {
			System.out.println("tools: " + JSON.toJSONString(ToolRegistry.getAllHandlers()));
            throw new ToolException("Tool not found: " + toolName, "TOOL_NOT_FOUND");
        }

        try {
            ToolResponse<Map<String, Object>> response = handler.execute(inputs);
            if (!response.isSuccess()) {
                throw new ToolException(response.getMessage(), response.getErrorCode());
            }
            return response.getData();
        } catch (ToolException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolException("Tool execution failed: " + e.getMessage(), "EXECUTION_ERROR");
        }
    }

    /**
     * 执行条件节点
     * @param node 条件节点
     * @param context 执行上下文
     * @return 条件评估结果
     */
    private Map<String, Object> evaluateCondition(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> result = new HashMap<>();
        
        if (node.getType() != NodeType.CONDITION) {
            result.put("evaluated", false);
            result.put("error", "Not a condition node");
            return result;
        }

        try {
            // 更新条件节点变量值
            List<ConditionCase> cases = node.getConditions();
            for (int i = 0; i < cases.size(); i++) {
                ConditionCase conditionCase = cases.get(i);
                if (conditionCase.getConditions().isEmpty()) {
                    // 空条件组是ELSE分支
                    continue;
                }
                
                boolean caseResult = true;
                for (Condition condition : conditionCase.getConditions()) {
                    // 更新左操作数值
                    VariableDefinition leftOp = condition.getLeftOperand();
                    if (leftOp != null && leftOp.getParent() != null) {
                        Object value = null;
                        if ("global".equals(leftOp.getParent())) {
                            value = context.getVariable(leftOp.getName());
                        } else {
                            // 从节点上下文获取值
                            WorkflowNode parentNode = context.getWorkflow().getNodeById(leftOp.getParent());
                            if (parentNode != null) {
                                value = parentNode.getFromContext(leftOp.getName());
                            }
                        }
                        leftOp.setValue(value);
                    }

                    // 更新右操作数值
                    VariableDefinition rightOp = condition.getRightOperand();
                    if (rightOp != null && "VARIABLE".equals(condition.getType()) && rightOp.getParent() != null) {
                        Object value = null;
                        if ("global".equals(rightOp.getParent())) {
                            value = context.getVariable(rightOp.getName());
                        } else {
                            // 从节点上下文获取值
                            WorkflowNode parentNode = context.getWorkflow().getNodeById(rightOp.getParent());
                            if (parentNode != null) {
                                value = parentNode.getFromContext(rightOp.getName());
                            }
                        }
                        rightOp.setValue(value);
                    }
                    
                    // 评估单个条件
                    boolean conditionResult = evaluateSingleCondition(condition);
                    if ("and".equalsIgnoreCase(conditionCase.getType())) {
                        caseResult = caseResult && conditionResult;
                        if (!caseResult) {
                            // Short circuit for AND
                            break;
                        }
                    } else if ("or".equalsIgnoreCase(conditionCase.getType())) {
                        caseResult = caseResult || conditionResult;
                        if (caseResult) {
                            // Short circuit for OR
                            break;
                        }
                    }
                }
                
                // 如果当前case的所有条件都满足，返回对应的case标识
                if (caseResult) {
                    result.put("evaluated", true);
                    result.put("matchedCase", "case" + (i + 1));
                    return result;
                }
            }
            
            // 所有条件组合都为false，使用else分支
            result.put("evaluated", false);
            result.put("matchedCase", "else");
        } catch (Exception e) {
            result.put("evaluated", false);
            result.put("error", e.getMessage());
            result.put("matchedCase", "else");
        }
        
        return result;
    }
    
    /**
     * 评估单个条件
     * @param condition 条件对象
     * @return 条件评估结果
     */
    private boolean evaluateSingleCondition(Condition condition) {
        Object leftValue = condition.getLeftOperand().getValue();
        Object rightValue = condition.getRightOperand().getValue();
        
        if (leftValue == null || rightValue == null) {
            return false;
        }
        
        ConditionOperator operator = ConditionOperator.valueOf(condition.getOperator());
        switch (operator) {
            case EQUALS:
                return leftValue.equals(rightValue);
            case NOT_EQUALS:
                return !leftValue.equals(rightValue);
            case GREATER_THAN:
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return ((Number) leftValue).doubleValue() > ((Number) rightValue).doubleValue();
                }
                return false;
            case LESS_THAN:
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return ((Number) leftValue).doubleValue() < ((Number) rightValue).doubleValue();
                }
                return false;
            case GREATER_THAN_OR_EQUALS:
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return ((Number) leftValue).doubleValue() >= ((Number) rightValue).doubleValue();
                }
                return false;
            case LESS_THAN_OR_EQUALS:
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return ((Number) leftValue).doubleValue() <= ((Number) rightValue).doubleValue();
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * Get the status of a workflow execution
     * @param executionId Execution ID
     * @return Current status or null if not found
     */
    public WorkflowStatus getWorkflowStatus(String executionId) {
        WorkflowContext context = activeWorkflows.get(executionId);
        return context != null ? context.getStatus() : null;
    }

    /**
     * Stop a running workflow
     * @param executionId Execution ID
     * @return true if workflow was stopped
     */
    public boolean stopWorkflow(String executionId) {
        WorkflowContext context = activeWorkflows.get(executionId);
        if (context != null) {
            context.setStatus(WorkflowStatus.SUSPENDED);
            return true;
        }
        return false;
    }
} 
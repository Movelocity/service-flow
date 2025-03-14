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
            workflowLogger.logWorkflowStart(context.getExecutionId(), workflow.getName());

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

			// 执行工作流，直到没有下一个节点
            while (currentNodeId != null) {
                WorkflowNode node = workflow.getNodeById(currentNodeId);
                if (node == null) {
                    throw new RuntimeException("Node not found: " + currentNodeId);
                }

                context.setCurrentNodeId(currentNodeId);
                long nodeStartTime = System.currentTimeMillis();

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
                NodeResult nodeResult = executeNode(node, context);
                
				if(nodeResult.hasError()) {
					// 出错直接中断
					throw new RuntimeException(nodeResult.getErrorCode() + " : " + nodeResult.getErrorMessage());
				}
				if(nodeResult.getOutputs() != null) {
					context.getVariables().putAll(nodeResult.getOutputs());
				}
                // 设置节点退出事件
                NodeExecutionEvent exitEvent = new NodeExecutionEvent(
                    context.getExecutionId(),
                    node.getId(),
                    node.getName(),
                    node.getType().toString(),
                    "EXIT"
                );
                exitEvent.setContextVariables(context.getVariables());
                debugService.sendDebugEvent(exitEvent);

                long nodeExecutionTime = System.currentTimeMillis() - nodeStartTime;

				// 收集节点执行参数信息
                Map<String, Object> nodeParameters = new HashMap<>();
                nodeParameters.put("toolName", node.getToolName());
                if (node.getType() == NodeType.FUNCTION && workflow.hasTool(node.getToolName())) {
                    ToolDefinition toolDef = workflow.getToolDefinition(node.getToolName());
                    nodeParameters.put("toolDescription", toolDef.getDescription());
                }
                // 记录节点执行情况
                workflowLogger.logNodeExecution(
                    context.getExecutionId(), 
                    workflow.getName(),
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
                if (nodeResult.getNodeType() == NodeType.CONDITION) {
                    // 对于条件节点，使用条件评估结果作为路径选择器
                    String path = (String) nodeResult.getMatchedCase();
                    currentNodeId = node.getNextNodes().get(path);
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
    private NodeResult executeNode(WorkflowNode node, WorkflowContext context) {
		NodeResult result = null;
        try {
            switch (node.getType()) {
                case START:
                    // 开始节点的工作已在 startWorkflow 中完成
					result = new NodeResult(NodeType.START);
                    break;
                    
                case FUNCTION:
					result = executeToolNode(node, context);
                    break;
                    
                case CONDITION:
					result = evaluateCondition(node, context);
                    break;
                    
                case END:
                    // 结束节点捕获最终状态
                    result = new NodeResult(NodeType.END);
                    break;
            }
        } catch (Exception e) {
			result = NodeResult.error(
				node.getType(), e.getClass().getSimpleName(), e.getMessage(), JSON.toJSONString(e.getStackTrace()));
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
     * Execute a tool with given inputs
     * @param toolName Name of the tool to execute
     * @param inputs Tool input parameters
     * @return Tool execution results
     */
    private Map<String, Object> executeTool(String toolName, Map<String, VariableDefinition> inputs) throws ToolException {
        ToolHandler handler = ToolRegistry.getHandler(toolName);
        if (handler == null) {
			System.out.println("tools: " + JSON.toJSONString(ToolRegistry.getAllHandlers()));
            throw new ToolException("Tool not found: " + toolName, "TOOL_NOT_FOUND");
        }

        try {
            // Convert Map<String, VariableDefinition> to Map<String, Object>
            Map<String, Object> inputValues = new HashMap<>();
            inputs.forEach((key, varDef) -> {
                if (varDef != null) {
                    inputValues.put(key, varDef.getValue());
                }
            });
            
            ToolResponse<Map<String, Object>> response = handler.execute(inputValues);
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

	private NodeResult executeToolNode(WorkflowNode node, WorkflowContext context) {
		NodeResult result = null;
		try {
			String toolName = node.getToolName();
			if (toolName == null || toolName.isEmpty()) {
				throw new RuntimeException("Tool name is empty");
			}
			// 准备工具输入参数
			Map<String, VariableDefinition> toolInputs = prepareToolInputs(node, context);
			// 执行工具并获取其输出
			Map<String, Object> toolResults = executeTool(toolName, toolInputs);

			Map<String, VariableDefinition> resultAsVars = new HashMap<>();
			Map<String, FieldDefinition> toolOutputSchema = ToolRegistry.getHandler(toolName).getDefinition().getOutputs();
			// 将 parent 设置成自己的id
			toolResults.forEach((key, value) -> {
				if (value == null) {
					// 没有这个输出，跳过
					return;
				}
				FieldDefinition fieldDef = toolOutputSchema.get(key);
				if (fieldDef == null) {
					// 没有这个输出定义，跳过
					return;
				}
				VariableDefinition varDef = VariableDefinition.fromFieldDefinition(fieldDef, node.getId());
				varDef.setName(key);
				varDef.setValue(value);
				resultAsVars.put(key, varDef);
			});
			result = new NodeResult(NodeType.FUNCTION, resultAsVars);
		} catch (Exception e) {
			result = NodeResult.error(NodeType.FUNCTION, e.getClass().getSimpleName(), e.getMessage(), JSON.toJSONString(e.getStackTrace()));
		}
		return result;
	}

    /**
     * 执行条件节点
     * @param node 条件节点
     * @param context 执行上下文
     * @return 条件评估结果
     */
    private NodeResult evaluateCondition(WorkflowNode node, WorkflowContext context) {
        if (node.getType() != NodeType.CONDITION) {
            return new NodeResult(NodeType.CONDITION, null, false);
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
                    // result.put("evaluated", true);
                    // result.put("matchedCase", "case" + (i + 1));
                    // return result;
					return new NodeResult(NodeType.CONDITION, "case" + (i + 1), true);
                }
            }
            
            // 所有条件组合都为false，使用else分支
            // result.put("evaluated", false);
            // result.put("matchedCase", "else");
			return new NodeResult(NodeType.CONDITION, "else", false);
        } catch (Exception e) {
            return NodeResult.error(NodeType.CONDITION, e.getClass().getSimpleName(), e.getMessage(), JSON.toJSONString(e.getStackTrace()));
        }
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
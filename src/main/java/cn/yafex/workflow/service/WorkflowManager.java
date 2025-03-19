package cn.yafex.workflow.service;

import cn.yafex.workflow.model.*;
import cn.yafex.workflow.execution.WorkflowContext;
import cn.yafex.workflow.execution.WorkflowStatus;
import cn.yafex.workflow.util.WorkflowLogger;
import cn.yafex.workflow.util.WorkflowLoader;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.core.ToolRegistry;
import cn.yafex.tools.schema.FieldDef;
import cn.yafex.tools.schema.VariableDef;
import cn.yafex.tools.schema.FieldType;
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
import java.util.Collection;

/**
 * 条件操作符枚举
 */
enum ConditionOperator {
    EQUALS("=="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN_OR_EQUALS("<="),
    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    IS_EMPTY("isEmpty"),
    IS_NOT_EMPTY("isNotEmpty");
    
    private final String symbol;
    
    ConditionOperator(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    /**
     * 从操作符符号获取对应的枚举值
     * @param symbol 操作符符号
     * @return 对应的枚举值
     */
    public static ConditionOperator fromSymbol(String symbol) {
        for (ConditionOperator operator : values()) {
            if (operator.getSymbol().equals(symbol)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("Unsupported operator: " + symbol);
    }
}

/**
 * Service for managing and executing workflows
 */
@Service
public class WorkflowManager {
    private final WorkflowLoader jsonFileHandler;
    private final WorkflowLogger workflowLogger;
    private final ExecutorService executorService;
    private final Map<String, WorkflowContext> activeWorkflows;
    private final WorkflowDebugService debugService;
	
    @Autowired
    public WorkflowManager(WorkflowLoader jsonFileHandler, WorkflowLogger workflowLogger, WorkflowDebugService debugService) {
        this.jsonFileHandler = jsonFileHandler;
        this.workflowLogger = workflowLogger;
        this.debugService = debugService;
        this.executorService = Executors.newCachedThreadPool();
        this.activeWorkflows = new ConcurrentHashMap<>();
    }

    /**
     * 启动工作流执行
     * @param workflowId 要执行的工作流ID
     * @param inputs 初始全局变量
     * @return 执行ID
     */
    public String startWorkflow(String workflowId, Map<String, Object> inputs) {
        try {
			System.out.println("startWorkflow: " + workflowId);
            Workflow workflow = jsonFileHandler.loadWorkflow(workflowId);
            WorkflowContext context = new WorkflowContext(workflowId);
            
            // 设置初始变量
			Map<String, VariableDef> initContext = new HashMap<>();
			// 检查所有参数
			Map<String, FieldDef> definedInputs = workflow.getInputs();
			for (Map.Entry<String, FieldDef> entry : definedInputs.entrySet()) {
				String key = entry.getKey();
				FieldDef value = entry.getValue();
				// 必填参数
				if (value.isRequired() && !inputs.containsKey(key)) {
					throw new RuntimeException("缺少必填参数: " + key);
				}
				VariableDef varDef = VariableDef.fromFieldDef(value, "global");
				// 如果输入参数为空，则使用默认值
				varDef.setValue(inputs.get(key)!=null?inputs.get(key):value.getDefaultValue());
				initContext.put(key, varDef);
			}
			context.setVariables(initContext);
            
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
                enterEvent.setContextVariables(context.getVariables());
                debugService.sendDebugEvent(enterEvent);
                
                // 根据节点类型执行节点
                NodeResult nodeResult = executeNode(node, context);
                
				if(nodeResult.hasError()) {
					// 出错直接中断
					throw new RuntimeException(nodeResult.getErrorCode() + " : " + nodeResult.getErrorMessage());
				}
				if(nodeResult.getOutputs() != null && node.getType() != NodeType.CONDITION) {
					// 非条件节点，将结果写入上下文
					context.getVariables().putAll(nodeResult.getOutputs());
				}

                long nodeExecutionTime = System.currentTimeMillis() - nodeStartTime;

                // 创建节点完成事件
                NodeExecutionEvent completeEvent = new NodeExecutionEvent(
                    context.getExecutionId(),
                    node.getId(),
                    node.getName(),
                    node.getType().toString(),
                    "COMPLETE"
                );
                completeEvent.setNodeResult(nodeResult.getOutputs());
                completeEvent.setDuration(nodeExecutionTime);
                
                // 使用NodeExecutionEvent作为日志输入源
                workflowLogger.logNodeExecution(completeEvent, workflow.getName());

                // 发送节点完成事件到调试服务
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
                case FUNCTION:
					result = executeToolNode(node, context);
                    break;
                    
                case CONDITION:
					result = evaluateCondition(node, context);
                    break;
                
				case START:
                case END:
                    result = new NodeResult(node.getType());
                    break;
            }
        } catch (Exception e) {
			result = NodeResult.error(
				node.getType(), e.getClass().getSimpleName(), e.getMessage(), JSON.toJSONString(e.getStackTrace()));
        }
        
        return result;
    }

    /**
     * 预先从上下文过滤出一批和工作流输入参数有关的变量
     * @param node 函数节点，带有inputMap表示变量映射
     * @param context 执行上下文
     * @return 准备好的工具输入
     */
    private Map<String, VariableDef> prepareToolInputs(WorkflowNode node, WorkflowContext context) {
        Map<String, VariableDef> toolInputs = context.getVariables();
        
        // 如果inputMap没有定义或为空，则使用所有上下文变量作为输入
        if (node.getInputMap() == null || node.getInputMap().isEmpty()) {
            return toolInputs;
        }
        Map<String, FieldDef> toolOutputSchema = ToolRegistry.getHandler(node.getToolName()).getDefinition().getOutputs();
        // 创建一个新的工具输入映射
        Map<String, VariableDef> filteredInputs = new HashMap<>();
        
        // 处理每个输入映射
        for (Map.Entry<String, VariableDef> entry : node.getInputMap().entrySet()) {
            String paramName = entry.getKey();
            VariableDef varDef = entry.getValue();
            
            if (varDef == null) {
                continue;
            }
            
            if ("CONSTANT".equals(varDef.getName())) {
                // 对于常量，直接使用值
                filteredInputs.put(paramName, varDef);
            } else {
                String varName = varDef.getName();
				String parent = varDef.getParent();
				VariableDef var = context.getVariable(varName, parent); // 从 context 获取变量
				if(var == null && toolOutputSchema.get(varName).isRequired()) {
					throw new RuntimeException("未找到必填变量: " + varName);
				}
				filteredInputs.put(paramName, var);
            }
        }
        
        return filteredInputs;
    }

    /**
     * 执行一个工具，给定输入
     * @param toolName 要执行的工具名称
     * @param inputs 工具输入参数
     * @return 工具执行结果
     */
    private Map<String, Object> executeTool(String toolName, Map<String, VariableDef> inputs) throws ToolException {
        ToolHandler handler = ToolRegistry.getHandler(toolName);
        if (handler == null) {
			System.out.println("tools: " + JSON.toJSONString(ToolRegistry.getAllHandlers()));
            throw new ToolException("Tool not found: " + toolName, "TOOL_NOT_FOUND");
        }

        try {
            // 将Map<String, VariableDef>转换为Map<String, Object>
            Map<String, Object> inputValues = new HashMap<>();
            inputs.forEach((key, varDef) -> {
                if (varDef != null) {
                    inputValues.put(key, varDef.getValue());
                }
            });
            
            ToolResponse<?> response = handler.execute(inputValues);
            if (!response.isSuccess()) {
                throw new ToolException(response.getMessage(), response.getErrorCode());
            }
            // 确保返回值是一个Map<String, Object>，对于List等非Map类型的返回值会自动包装
            return ToolResponse.ensureMapResponse(response.getData());
        } catch (ToolException e) {
            throw e;
        } catch (Exception e) {
            throw new ToolException("Tool execution failed: " + e.getMessage(), "EXECUTION_ERROR");
        }
    }
	
	/**
	 * 执行工具节点
	 * @param node 工具节点
	 * @param context 执行上下文
	 * @return 节点执行结果
	 */
	private NodeResult executeToolNode(WorkflowNode node, WorkflowContext context) {
		NodeResult result = null;
		try {
			String toolName = node.getToolName();
			if (toolName == null || toolName.isEmpty()) {
				throw new RuntimeException("Tool name is empty");
			}
			// 准备工具输入参数
			Map<String, VariableDef> toolInputs = prepareToolInputs(node, context);
			// 执行工具并获取其输出
			Map<String, Object> toolResults = executeTool(toolName, toolInputs);

			Map<String, VariableDef> resultAsVars = new HashMap<>();
			Map<String, FieldDef> toolOutputSchema = ToolRegistry.getHandler(toolName).getDefinition().getOutputs();
			
			// 对于包装的结果需要特殊处理
			// 如果只有一个输出字段，并且是array类型，同时工具结果包含"items"字段，则直接使用items对应的值
			// 这种情况是当工具返回了Collection，系统将其包装成了带items字段的map
			if (toolOutputSchema.size() == 1) {
				for (Map.Entry<String, FieldDef> entry : toolOutputSchema.entrySet()) {
					FieldDef fieldDef = entry.getValue();
					if (fieldDef.getType() == FieldType.ARRAY && toolResults.containsKey("items")) {
						VariableDef varDef = VariableDef.fromFieldDef(fieldDef, node.getId());
						varDef.setName(entry.getKey());
						varDef.setValue(toolResults.get("items"));
						resultAsVars.put(entry.getKey(), varDef);
						// 跳过常规处理逻辑
						result = new NodeResult(NodeType.FUNCTION, resultAsVars);
						return result;
					}
				}
			}
			
			// 将 parent 设置成自己的id
			toolResults.forEach((key, value) -> {
				if (value == null) {
					// 没有这个输出，跳过
					return;
				}
				FieldDef fieldDef = toolOutputSchema.get(key);
				if (fieldDef == null) {
					// 没有这个输出定义，跳过
					return;
				}
				VariableDef varDef = VariableDef.fromFieldDef(fieldDef, node.getId());
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
     * @param context 执行上下文，任何变量只能从这里获取，不能从节点上获取
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
                    VariableDef leftOp = condition.getLeftOperand();
                    if (leftOp != null) {
						// 填充值
                        VariableDef var = context.getVariable(leftOp.getName(), leftOp.getParent());
                        leftOp.setValue(var.getValue());
                    }

                    // 更新右操作数值
                    VariableDef rightOp = condition.getRightOperand();
                    if (rightOp != null && "VARIABLE".equals(condition.getType())) {
						// 填充值
                        VariableDef var = context.getVariable(rightOp.getName(), rightOp.getParent());
                        rightOp.setValue(var.getValue());
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
					return new NodeResult(NodeType.CONDITION, "case" + (i + 1), true);
                }
            }
            
            // 所有条件组合都为false，使用else分支
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
        
        // Special case for isEmpty and isNotEmpty operators
        ConditionOperator operator = ConditionOperator.fromSymbol(condition.getOperator());
        if (operator == ConditionOperator.IS_EMPTY) {
            return leftValue == null || 
                   (leftValue instanceof String && ((String) leftValue).isEmpty()) ||
                   (leftValue instanceof Collection && ((Collection<?>) leftValue).isEmpty());
        }
        
        if (operator == ConditionOperator.IS_NOT_EMPTY) {
            return leftValue != null && 
                   !((leftValue instanceof String && ((String) leftValue).isEmpty()) ||
                     (leftValue instanceof Collection && ((Collection<?>) leftValue).isEmpty()));
        }
        
        // For other operators, both operands must have values
        if (leftValue == null || rightValue == null) {
            return false;
        }
        
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
            case CONTAINS:
                if (leftValue instanceof String && rightValue instanceof String) {
                    return ((String) leftValue).contains((String) rightValue);
                }
                return false;
            case NOT_CONTAINS:
                if (leftValue instanceof String && rightValue instanceof String) {
                    return !((String) leftValue).contains((String) rightValue);
                }
                return false;
            case STARTS_WITH:
                if (leftValue instanceof String && rightValue instanceof String) {
                    return ((String) leftValue).startsWith((String) rightValue);
                }
                return false;
            case ENDS_WITH:
                if (leftValue instanceof String && rightValue instanceof String) {
                    return ((String) leftValue).endsWith((String) rightValue);
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * 获取工作流执行状态
     * @param executionId 执行ID
     * @return 当前状态或null如果未找到
     */
    public WorkflowStatus getWorkflowStatus(String executionId) {
        WorkflowContext context = activeWorkflows.get(executionId);
        return context != null ? context.getStatus() : null;
    }

    /**
     * 停止一个正在运行的工作流
     * @param executionId 执行ID
     * @return 如果工作流被停止则返回true
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
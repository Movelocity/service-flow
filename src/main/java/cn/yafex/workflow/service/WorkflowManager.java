package cn.yafex.workflow.service;

import cn.yafex.workflow.model.*;
import cn.yafex.workflow.execution.WorkflowContext;
import cn.yafex.workflow.execution.WorkflowStatus;
import cn.yafex.workflow.util.WorkflowLogger;
import cn.yafex.workflow.util.JsonFileHandler;
import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for managing and executing workflows
 */
@Service
public class WorkflowManager {
    private final JsonFileHandler jsonFileHandler;
    private final WorkflowLogger workflowLogger;
    private final ExecutorService executorService;
    private final Map<String, WorkflowContext> activeWorkflows;
    private final Map<String, ToolHandler> toolHandlers;

    @Autowired
    public WorkflowManager(JsonFileHandler jsonFileHandler, WorkflowLogger workflowLogger) {
        this.jsonFileHandler = jsonFileHandler;
        this.workflowLogger = workflowLogger;
        this.executorService = Executors.newCachedThreadPool();
        this.activeWorkflows = new ConcurrentHashMap<>();
        this.toolHandlers = new ConcurrentHashMap<>();
    }

    /**
     * Register a tool handler
     * @param handler The tool handler to register
     * @throws IllegalArgumentException if a handler with the same name already exists
     */
    public void registerToolHandler(ToolHandler handler) {
        ToolDefinition definition = handler.getDefinition();
        String toolName = definition.getName();
        
        if (toolHandlers.containsKey(toolName)) {
            throw new IllegalArgumentException("工具已存在注册: " + toolName);
        }
        
        toolHandlers.put(toolName, handler);
    }

    /**
     * Check if a tool handler is registered
     * @param toolName The name of the tool
     * @return true if registered, false otherwise
     */
    public boolean hasToolHandler(String toolName) {
        return toolHandlers.containsKey(toolName);
    }

    /**
     * Get a tool handler by name
     * @param toolName The name of the tool
     * @return The tool handler, or null if not found
     */
    public ToolHandler getToolHandler(String toolName) {
        return toolHandlers.get(toolName);
    }

    /**
     * Start execution of a workflow
     * @param workflowId ID of the workflow to execute
     * @param initialVariables Initial global variables
     * @return Execution ID
     */
    public String startWorkflow(String workflowId, Map<String, Object> initialVariables) {
        try {
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
     * Execute a workflow
     * @param workflow Workflow to execute
     * @param context Execution context
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
                
                // Execute node based on its type
                Map<String, Object> nodeResult = executeNode(node, context);
                
                // Update node context for function nodes
                if (node.getType() == NodeType.FUNCTION) {
                    node.setContext(new HashMap<>(nodeResult));
                }

                Map<String, Object> nodeParameters = new HashMap<>();
                nodeParameters.put("toolName", node.getToolName());
                if (node.getType() == NodeType.FUNCTION && workflow.hasTool(node.getToolName())) {
                    ToolDefinition toolDef = workflow.getToolDefinition(node.getToolName());
                    nodeParameters.put("toolDescription", toolDef.getDescription());
                }

                long nodeDuration = System.currentTimeMillis() - nodeStartTime;
                workflowLogger.logNodeExecution(
                    context.getExecutionId(),
                    node.getName(),
                    node.getType().toString(),
                    nodeParameters,
                    nodeResult,
                    nodeDuration
                );

                // Determine next node
                if (node.getType() == NodeType.END) {
                    // Copy relevant context variables to workflow outputs
                    workflow.getOutputs().putAll(context.getVariables());
                    currentNodeId = null;
                } else if (node.getType() == NodeType.CONDITION) {
                    boolean condition = evaluateCondition(node, context);
                    currentNodeId = condition ? 
                        node.getNextNodes().get("true") : 
                        node.getNextNodes().get("false");
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
                context.getStatus().toString(),
                duration
            );
            activeWorkflows.remove(context.getExecutionId());
        }
    }

    /**
     * Execute a single node
     * @param node Node to execute
     * @param context Execution context
     * @return Node execution result
     */
    private Map<String, Object> executeNode(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (node.getType()) {
                case START:
                    // Start node initializes global variables
                    result.putAll(context.getVariables());
                    break;
                    
                case FUNCTION:
                    if (node.getToolName() != null) {
                        // Execute the tool and get its outputs
                        result = executeTool(node.getToolName(), context.getVariables());
                        // Update context with tool outputs
                        context.getVariables().putAll(result);
                    }
                    break;
                    
                case CONDITION:
                    // Condition nodes don't modify the context
                    result.put("evaluated", evaluateCondition(node, context));
                    break;
                    
                case END:
                    // End node captures final state
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
     * Execute a tool with given inputs
     * @param toolName Name of the tool to execute
     * @param inputs Tool input parameters
     * @return Tool execution results
     */
    private Map<String, Object> executeTool(String toolName, Map<String, Object> inputs) throws ToolException {
        ToolHandler handler = toolHandlers.get(toolName);
        if (handler == null) {
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
     * Evaluate a condition node
     * @param node Condition node
     * @param context Execution context
     * @return Condition result
     */
    private boolean evaluateCondition(WorkflowNode node, WorkflowContext context) {
        if (node.getType() != NodeType.CONDITION) {
            return true;
        }

        // Update variable values from context
        for (ConditionCase conditionCase : node.getConditions()) {
            for (Condition condition : conditionCase.getConditions()) {
                // Update left operand value if it's a variable
                VariableDefinition leftOp = condition.getLeftOperand();
                if (leftOp != null && leftOp.getParent() != null) {
                    Object value = null;
                    if ("global".equals(leftOp.getParent())) {
                        value = context.getVariable(leftOp.getName());
                    } else {
                        // Get value from node context
                        WorkflowNode parentNode = context.getWorkflow().getNodeById(leftOp.getParent());
                        if (parentNode != null) {
                            value = parentNode.getFromContext(leftOp.getName());
                        }
                    }
                    leftOp.setValue(value);
                }

                // Update right operand value if it's a variable
                VariableDefinition rightOp = condition.getRightOperand();
                if (rightOp != null && "VARIABLE".equals(condition.getType()) && rightOp.getParent() != null) {
                    Object value = null;
                    if ("global".equals(rightOp.getParent())) {
                        value = context.getVariable(rightOp.getName());
                    } else {
                        // Get value from node context
                        WorkflowNode parentNode = context.getWorkflow().getNodeById(rightOp.getParent());
                        if (parentNode != null) {
                            value = parentNode.getFromContext(rightOp.getName());
                        }
                    }
                    rightOp.setValue(value);
                }
            }
        }

        // Evaluate conditions
        return node.evaluateConditions();
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
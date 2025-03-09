package cn.yafex.workflow.service;

import cn.yafex.workflow.model.Workflow;
import cn.yafex.workflow.model.WorkflowNode;
import cn.yafex.workflow.model.NodeType;
import cn.yafex.workflow.execution.WorkflowContext;
import cn.yafex.workflow.execution.WorkflowStatus;
import cn.yafex.workflow.util.WorkflowLogger;
import cn.yafex.workflow.util.JsonFileHandler;
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

    @Autowired
    public WorkflowManager(JsonFileHandler jsonFileHandler, WorkflowLogger workflowLogger) {
        this.jsonFileHandler = jsonFileHandler;
        this.workflowLogger = workflowLogger;
        this.executorService = Executors.newCachedThreadPool();
        this.activeWorkflows = new ConcurrentHashMap<>();
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
            while (currentNodeId != null) {
                WorkflowNode node = workflow.getNodeById(currentNodeId);
                if (node == null) {
                    throw new RuntimeException("Node not found: " + currentNodeId);
                }

                context.setCurrentNodeId(currentNodeId);
                long nodeStartTime = System.currentTimeMillis();
                
                // Execute node based on its type
                Map<String, Object> nodeResult = executeNode(node, context);
                Map<String, Object> nodeParameters = new HashMap<>();
                nodeParameters.put("toolName", node.getToolName());
                nodeParameters.put("toolDescription", node.getToolDescription());
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
        // This is a placeholder implementation
        // In a real system, this would handle different node types and their specific logic
        Map<String, Object> result = new HashMap<>();
        result.put("status", "executed");
        return result;
    }

    /**
     * Evaluate a condition node
     * @param node Condition node
     * @param context Execution context
     * @return Condition result
     */
    private boolean evaluateCondition(WorkflowNode node, WorkflowContext context) {
        // This is a placeholder implementation
        // In a real system, this would evaluate the condition based on the node's parameters
        return true;
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
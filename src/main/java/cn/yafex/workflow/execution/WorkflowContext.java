package cn.yafex.workflow.execution;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Class representing the execution context of a workflow
 */
public class WorkflowContext {
    private String executionId;
    private String workflowId;
    private Map<String, Object> variables;
    private String currentNodeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WorkflowStatus status;

    public WorkflowContext(String workflowId) {
        this.executionId = UUID.randomUUID().toString();
        this.workflowId = workflowId;
        this.variables = new HashMap<>();
        this.startTime = LocalDateTime.now();
        this.status = WorkflowStatus.RUNNING;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    /**
     * Set a variable in the context
     * @param key Variable name
     * @param value Variable value
     */
    public void setVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    /**
     * Get a variable from the context
     * @param key Variable name
     * @return Variable value
     */
    public Object getVariable(String key) {
        return this.variables.get(key);
    }
} 
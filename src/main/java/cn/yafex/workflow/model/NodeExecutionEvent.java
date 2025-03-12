package cn.yafex.workflow.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents an event during node execution for debugging purposes
 */
public class NodeExecutionEvent {
    private String executionId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String eventType; // ENTER or COMPLETE
    private Map<String, Object> nodeContext;
    private Map<String, Object> globalVariables;
    private LocalDateTime timestamp;
    private Long duration; // Only for COMPLETE events

    public NodeExecutionEvent(String executionId, String nodeId, String nodeName, String nodeType, String eventType) {
        this.executionId = executionId;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getExecutionId() {
        return executionId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getNodeContext() {
        return nodeContext;
    }

    public void setNodeContext(Map<String, Object> nodeContext) {
        this.nodeContext = nodeContext;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
} 
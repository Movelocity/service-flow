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
	private Map<String, Object> nodeResult;
    private Map<String, Object> contextVariables;
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

	public Map<String, Object> getNodeResult() {
		return nodeResult;
	}

	public void setNodeResult(Map<String, Object> nodeResult) {
		this.nodeResult = nodeResult;
	}

    public Map<String, Object> getContextVariables() {
        return contextVariables;
    }

    public void setContextVariables(Map<String, Object> contextVariables) {
        this.contextVariables = contextVariables;
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
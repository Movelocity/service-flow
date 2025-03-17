package cn.yafex.workflow.model;

import java.time.LocalDateTime;
import java.util.Map;

import cn.yafex.tools.schema.VariableDefinition;

/**
 * 工作流执行过程中的调试信息，可以用于前端调试和后端存日志
 */
public class NodeExecutionEvent {
    private String executionId;
    private String nodeId;
    private String nodeName;
    private String nodeType;
    private String eventType; // ENTER or COMPLETE
	private Map<String, VariableDefinition> nodeResult;
    private Map<String, VariableDefinition> contextVariables;
    private LocalDateTime timestamp;
    private Long duration; // 仅在COMPLETE事件中使用

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

	public Map<String, VariableDefinition> getNodeResult() {
		return nodeResult;
	}

	public void setNodeResult(Map<String, VariableDefinition> nodeResult) {
		this.nodeResult = nodeResult;
	}

    public Map<String, VariableDefinition> getContextVariables() {
        return contextVariables;
    }

    public void setContextVariables(Map<String, VariableDefinition> contextVariables) {
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
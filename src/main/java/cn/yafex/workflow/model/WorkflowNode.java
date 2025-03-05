package cn.yafex.workflow.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Base class for all workflow nodes
 */
public class WorkflowNode {
    private String id;
    private String name;
    private NodeType type;
    private Map<String, Object> parameters;
    private Map<String, String> nextNodes; // key: condition/default, value: next node id

    public WorkflowNode() {
        this.parameters = new HashMap<>();
        this.nextNodes = new HashMap<>();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(Map<String, String> nextNodes) {
        this.nextNodes = nextNodes;
    }
} 
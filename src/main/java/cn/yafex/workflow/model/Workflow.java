package cn.yafex.workflow.model;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Class representing a complete workflow definition
 */
public class Workflow {
    private String id;
    private String name;
    private String description;
    private Map<String, Object> globalVariables;
    private List<WorkflowNode> nodes;
    private String startNodeId;
    private boolean isActive;

    public Workflow() {
        this.globalVariables = new HashMap<>();
        this.nodes = new ArrayList<>();
        this.isActive = true;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public List<WorkflowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WorkflowNode> nodes) {
        this.nodes = nodes;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Add a node to the workflow
     * @param node The node to add
     */
    public void addNode(WorkflowNode node) {
        this.nodes.add(node);
    }

    /**
     * Get a node by its ID
     * @param nodeId The ID of the node to find
     * @return The node if found, null otherwise
     */
    public WorkflowNode getNodeById(String nodeId) {
        return this.nodes.stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }
} 
package cn.yafex.workflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;

/**
 * Base class for all workflow nodes
 */
public class WorkflowNode {
    private String id;
    private String name;
    private String description;
    private NodeType type;
    private Map<String, String> nextNodes; // key: condition/default, value: next node id
    private Position position;      // 节点在画布上的位置，用于编辑器
    private String toolName;        // Only used for FUNCTION type nodes
    private Map<String, Object> context; // Node context for function outputs

    public WorkflowNode() {
        this.nextNodes = new HashMap<>();
        this.position = new Position(0, 0);
        this.context = new HashMap<>();
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

    public NodeType getType() {
        return type;
    }

    @JsonCreator
    public void setType(String type) {
        try {
            this.type = NodeType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.type = NodeType.FUNCTION; // Default to FUNCTION if unknown
        }
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Map<String, String> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(Map<String, String> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Inner class representing a node's position on the canvas
     */
    public static class Position {
        private double x;
        private double y;

        public Position() {
            this(0, 0);
        }

        @JsonCreator
        public Position(@JsonProperty("x") double x, @JsonProperty("y") double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    /**
     * Add a value to the node's context
     * @param key Context variable name
     * @param value Context variable value
     */
    public void addToContext(String key, Object value) {
        this.context.put(key, value);
    }

    /**
     * Get a value from the node's context
     * @param key Context variable name
     * @return Context variable value or null if not found
     */
    public Object getFromContext(String key) {
        return this.context.get(key);
    }

    /**
     * Clear the node's context
     */
    public void clearContext() {
        this.context.clear();
    }
} 
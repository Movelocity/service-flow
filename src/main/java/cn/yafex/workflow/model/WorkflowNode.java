package cn.yafex.workflow.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;
import cn.yafex.tools.schema.FieldDefinition;

/**
 * Base class for all workflow nodes
 */
public class WorkflowNode {
    private String id;
    private String name;
    private String description;
    private NodeType type;
    private Map<String, String> nextNodes; // key: condition/default, value: next node id
    private Position position;
    private String toolName;
    private String toolDescription;
    // inputs and outputs
    private Map<String, FieldDefinition> inputs;
    private Map<String, FieldDefinition> outputs;

    public WorkflowNode() {
        this.nextNodes = new HashMap<>();
        this.position = new Position(0, 0);
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

    public String getToolDescription() {
        return toolDescription;
    }

    public void setToolDescription(String toolDescription) {
        this.toolDescription = toolDescription;
    }

    public Map<String, FieldDefinition> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, FieldDefinition> inputs) {
        this.inputs = inputs;
    }

    public Map<String, FieldDefinition> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, FieldDefinition> outputs) {
        this.outputs = outputs;
    }
} 
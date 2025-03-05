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
    private NodeType type;
    private Map<String, Object> parameters;
    private Map<String, String> nextNodes; // key: condition/default, value: next node id
    private Position position;

    public WorkflowNode() {
        this.parameters = new HashMap<>();
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
} 
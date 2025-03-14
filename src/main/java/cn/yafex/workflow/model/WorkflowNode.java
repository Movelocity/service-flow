package cn.yafex.workflow.model;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import cn.yafex.tools.schema.VariableDefinition;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

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
    private List<ConditionCase> conditions; // Only used for CONDITION type nodes
    private Map<String, VariableDefinition> inputMap; // Input parameter mappings for FUNCTION type nodes

    public WorkflowNode() {
        this.nextNodes = new HashMap<>();
        this.position = new Position(0, 0);
        this.conditions = new ArrayList<>();
        this.inputMap = new HashMap<>();
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

    @JSONCreator
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

        @JSONCreator
        public Position(@JSONField(name = "x") double x, @JSONField(name = "y") double y) {
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

    public List<ConditionCase> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionCase> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    /**
     * Get the input parameter mappings for this node
     * @return Map of parameter name to variable definition
     */
    public Map<String, VariableDefinition> getInputMap() {
        return inputMap;
    }

    /**
     * Set the input parameter mappings for this node
     * @param inputMap Map of parameter name to variable definition
     */
    public void setInputMap(Map<String, VariableDefinition> inputMap) {
        this.inputMap = inputMap != null ? inputMap : new HashMap<>();
    }

    /**
     * Add a parameter mapping to this node's inputMap
     * @param paramName Parameter name
     * @param variableDefinition Variable definition for the parameter
     */
    public void addInputMapping(String paramName, VariableDefinition variableDefinition) {
        if (paramName != null && variableDefinition != null) {
            this.inputMap.put(paramName, variableDefinition);
        }
    }

    /**
     * Get a parameter mapping from this node's inputMap
     * @param paramName Parameter name
     * @return Variable definition for the parameter or null if not found
     */
    public VariableDefinition getInputMapping(String paramName) {
        return this.inputMap.get(paramName);
    }

    /**
     * Add a condition case to this node
     * @param conditionCase The condition case to add
     */
    public void addConditionCase(ConditionCase conditionCase) {
        if (conditionCase != null) {
            this.conditions.add(conditionCase);
        }
    }

    /**
     * Evaluates all condition cases for this node
     * @return true if any condition case evaluates to true, false otherwise
     */
    public boolean evaluateConditions() {
        if (type != NodeType.CONDITION || conditions.isEmpty()) {
            return true;
        }

        // For condition nodes, evaluate each case
        for (ConditionCase conditionCase : conditions) {
            if (conditionCase.evaluate()) {
                return true;
            }
        }
        
        return false;
    }
} 
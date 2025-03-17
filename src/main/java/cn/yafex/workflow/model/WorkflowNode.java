package cn.yafex.workflow.model;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import cn.yafex.tools.schema.VariableDefinition;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 工作流节点的基本类型
 */
public class WorkflowNode {
    private String id;
    private String name;
    private String description;
    private NodeType type;
    private Map<String, String> nextNodes; // key: condition/default, value: next node id
    private Position position;      // 节点在画布上的位置，用于可视化
    private String toolName;        // 仅用于FUNCTION类型节点
    private List<ConditionCase> conditions; // 仅用于CONDITION类型节点
    private Map<String, VariableDefinition> inputMap; // 用于FUNCTION类型节点的输入参数映射

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
     * 内部类，表示节点在画布上的位置
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
     * 获取此节点的输入参数映射
     * @return 参数名称到变量定义的映射
     */
    public Map<String, VariableDefinition> getInputMap() {
        return inputMap;
    }

    /**
     * 设置此节点的输入参数映射
     * @param inputMap 参数名称到变量定义的映射
     */
    public void setInputMap(Map<String, VariableDefinition> inputMap) {
        this.inputMap = inputMap != null ? inputMap : new HashMap<>();
    }

    /**
     * 添加一个参数映射到此节点的inputMap
     * @param paramName 参数名称
     * @param variableDefinition 参数的变量定义
     */
    public void addInputMapping(String paramName, VariableDefinition variableDefinition) {
        if (paramName != null && variableDefinition != null) {
            this.inputMap.put(paramName, variableDefinition);
        }
    }

    /**
     * 从此节点的inputMap获取一个参数映射
     * @param paramName 参数名称
     * @return 参数的变量定义或null如果未找到
     */
    public VariableDefinition getInputMapping(String paramName) {
        return this.inputMap.get(paramName);
    }

    /**
     * 添加一个条件案例到此节点
     * @param conditionCase 要添加的条件案例
     */
    public void addConditionCase(ConditionCase conditionCase) {
        if (conditionCase != null) {
            this.conditions.add(conditionCase);
        }
    }

    /**
     * 评估此节点的所有条件案例
     * @return 如果任何条件案例评估为true，则返回true，否则返回false
     */
    public boolean evaluateConditions() {
        if (type != NodeType.CONDITION || conditions.isEmpty()) {
            return true;
        }

        // 对于条件节点，评估每个案例
        for (ConditionCase conditionCase : conditions) {
            if (conditionCase.evaluate()) {
                return true;
            }
        }
        
        return false;
    }
} 
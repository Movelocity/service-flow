package cn.yafex.workflow.model;

import cn.yafex.tools.schema.VariableDef;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Represents a single condition in a workflow condition node.
 * This class matches the frontend Condition interface structure.
 */
public class Condition {
    @JSONField(name = "leftOperand")
    private VariableDef leftOperand;

    @JSONField(name = "operator")
    private String operator;

    @JSONField(name = "rightOperand")
    private VariableDef rightOperand;

    @JSONField(name = "type")
    private String type;  // "VARIABLE" or "CONSTANT"

    public Condition() {
        // Default constructor for JSON serialization
    }

    public Condition(VariableDef leftOperand, String operator, VariableDef rightOperand, String type) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.type = type;
    }

    // Getters and setters
    public VariableDef getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(VariableDef leftOperand) {
        this.leftOperand = leftOperand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public VariableDef getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(VariableDef rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
	 * 基于当前Condition的配置执行单个条件判断
     * @return 条件是否成立。有可能报错，请注意处理
     */
    public boolean evaluate() {
        Object leftValue = leftOperand.getValue();
        Object rightValue = type.equals("CONSTANT") ? rightOperand.getValue() : rightOperand.getValue();

        if (leftValue == null || rightValue == null) {
            return false;
        }

        // Convert values to comparable types if necessary
        leftValue = convertToAppropriateType(leftValue, leftOperand.getType());
        rightValue = convertToAppropriateType(rightValue, rightOperand.getType());

        return compareValues(leftValue, rightValue, operator);
    }

    private Object convertToAppropriateType(Object value, String type) {
        if (value == null) return null;
        
        try {
            switch (type.toLowerCase()) {
                case "string":
                    return value.toString();
                case "number":
                    if (value instanceof String) {
                        return Double.parseDouble((String) value);
                    } else if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    break;
                case "boolean":
                    if (value instanceof String) {
                        return Boolean.parseBoolean((String) value);
                    }
                    break;
            }
        } catch (Exception e) {
            // Log error and return original value if conversion fails
            System.err.println("类型转换错误: " + e.getMessage());
        }
        return value;
    }

	@SuppressWarnings("unchecked")
    private boolean compareValues(Object left, Object right, String operator) {
        if (left == null || right == null) return false;

        switch (operator) {
            case "==":
                return left.equals(right);
            case "!=":
                return !left.equals(right);
            case ">":
                if (left instanceof Comparable && right instanceof Comparable) {
                    return ((Comparable) left).compareTo(right) > 0;
                }
                break;
            case ">=":
                if (left instanceof Comparable && right instanceof Comparable) {
                    return ((Comparable) left).compareTo(right) >= 0;
                }
                break;
            case "<":
                if (left instanceof Comparable && right instanceof Comparable) {
                    return ((Comparable) left).compareTo(right) < 0;
                }
                break;
            case "<=":
                if (left instanceof Comparable && right instanceof Comparable) {
                    return ((Comparable) left).compareTo(right) <= 0;
                }
                break;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Condition{leftOperand=%s, operator='%s', rightOperand=%s, type='%s'}", 
            leftOperand, operator, rightOperand, type);
    }
} 
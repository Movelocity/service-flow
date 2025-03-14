package cn.yafex.workflow.model;

import cn.yafex.tools.schema.VariableDefinition;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Represents a single condition in a workflow condition node.
 * This class matches the frontend Condition interface structure.
 */
public class Condition {
    @JSONField(name = "leftOperand")
    private VariableDefinition leftOperand;

    @JSONField(name = "operator")
    private String operator;

    @JSONField(name = "rightOperand")
    private VariableDefinition rightOperand;

    @JSONField(name = "type")
    private String type;  // "VARIABLE" or "CONSTANT"

    public Condition() {
        // Default constructor for JSON serialization
    }

    public Condition(VariableDefinition leftOperand, String operator, VariableDefinition rightOperand, String type) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.type = type;
    }

    // Getters and setters
    public VariableDefinition getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(VariableDefinition leftOperand) {
        this.leftOperand = leftOperand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public VariableDefinition getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(VariableDefinition rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Evaluates this condition based on the current values of its operands
     * @return true if the condition is satisfied, false otherwise
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
            System.err.println("Error converting value: " + e.getMessage());
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
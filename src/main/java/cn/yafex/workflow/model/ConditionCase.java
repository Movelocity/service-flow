package cn.yafex.workflow.model;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a group of conditions in a workflow condition node.
 * This class matches the frontend ConditionCase interface structure.
 */
public class ConditionCase {
    @JSONField(name = "conditions")
    private List<Condition> conditions;

    @JSONField(name = "type")
    private String type;  // "and" or "or"

    @JSONField(name = "hint")
    private String hint;

    public ConditionCase() {
        this.conditions = new ArrayList<>();
    }

    public ConditionCase(String type) {
        this.conditions = new ArrayList<>();
        this.type = type;
    }

    // Getters and setters
    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    /**
     * Add a condition to this case
     * @param condition The condition to add
     */
    public void addCondition(Condition condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }
    }

    /**
     * Evaluates all conditions in this case according to the type (AND/OR)
     * @return true if the conditions are satisfied according to the type, false otherwise
     */
    public boolean evaluate() {
        if (conditions.isEmpty()) {
            return true;
        }

        boolean result = type.equalsIgnoreCase("and");
        
        for (Condition condition : conditions) {
            boolean conditionResult = condition.evaluate();
            
            if (type.equalsIgnoreCase("and")) {
                result &= conditionResult;
                if (!result) {
                    // Short circuit for AND
                    return false;
                }
            } else if (type.equalsIgnoreCase("or")) {
                result |= conditionResult;
                if (result) {
                    // Short circuit for OR
                    return true;
                }
            }
        }
        
        return result;
    }

    @Override
    public String toString() {
        return String.format("ConditionCase{type='%s', hint='%s', conditions=%s}", 
            type, hint, conditions);
    }
} 
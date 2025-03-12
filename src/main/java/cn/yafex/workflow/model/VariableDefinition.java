package cn.yafex.workflow.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Represents a variable definition in the workflow system.
 * This class matches the frontend VariableDefinition interface structure.
 */
public class VariableDefinition {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "type")
    private String type;

    @JSONField(name = "description")
    private String description;

    @JSONField(name = "defaultValue")
    private Object defaultValue;

    @JSONField(name = "value")
    private Object value;

    @JSONField(name = "parent")
    private String parent;

    public VariableDefinition() {
        // Default constructor for JSON serialization
    }

    public VariableDefinition(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return String.format("VariableDefinition{name='%s', type='%s', description='%s', value=%s}", 
            name, type, description, value);
    }
} 
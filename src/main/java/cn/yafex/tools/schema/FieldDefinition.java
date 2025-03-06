package cn.yafex.tools.schema;

import java.util.List;
import java.util.Map;

/**
 * Defines the structure and constraints of a field in a tool's input or output
 */
public class FieldDefinition {
    private String name;
    private String description;
    private FieldType type;
    private boolean required;
    private Object defaultValue;
    private Map<String, Object> constraints;
    private List<String> enumValues;
    private Map<String, FieldDefinition> properties;
    private FieldDefinition itemDefinition;

    public FieldDefinition(String name, String description, FieldType type, boolean required,
                         Object defaultValue, Map<String, Object> constraints) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.constraints = constraints;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public FieldType getType() { return type; }
    public void setType(FieldType type) { this.type = type; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Object getDefaultValue() { return defaultValue; }
    public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }

    public List<String> getEnumValues() { return enumValues; }
    public void setEnumValues(List<String> enumValues) { this.enumValues = enumValues; }

    public Map<String, FieldDefinition> getProperties() { return properties; }
    public void setProperties(Map<String, FieldDefinition> properties) { this.properties = properties; }

    public FieldDefinition getItemDefinition() { return itemDefinition; }
    public void setItemDefinition(FieldDefinition itemDefinition) { this.itemDefinition = itemDefinition; }
} 
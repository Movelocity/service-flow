package cn.yafex.tools.schema;

import java.util.List;
import java.util.Map;

/**
 * Defines the structure and constraints of a field in a tool's input or output
 */
public class FieldDefinition {
    private final String name;
    private final String description;
    private final FieldType type;
    private final boolean required;
    private final String defaultValue;
    private final String pattern;
    private Map<String, Object> constraints;
    private List<String> enumValues;
    private Map<String, FieldDefinition> properties;
    private FieldDefinition itemDefinition;

    public FieldDefinition(
        String name,
        String description,
        FieldType type,
        boolean required,
        String defaultValue,
        String pattern
    ) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { }

    public String getDescription() { return description; }
    public void setDescription(String description) { }

    public FieldType getType() { return type; }
    public void setType(FieldType type) { }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }

    public List<String> getEnumValues() { return enumValues; }
    public void setEnumValues(List<String> enumValues) { this.enumValues = enumValues; }

    public Map<String, FieldDefinition> getProperties() { return properties; }
    public void setProperties(Map<String, FieldDefinition> properties) { this.properties = properties; }

    public FieldDefinition getItemDefinition() { return itemDefinition; }
    public void setItemDefinition(FieldDefinition itemDefinition) { this.itemDefinition = itemDefinition; }
} 
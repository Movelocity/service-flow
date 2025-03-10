package cn.yafex.tools.schema;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * Defines the structure and constraints of a field in a tool's input or output
 */
public class FieldDefinition {
    @JSONField(name = "name")
    private final String name;

    @JSONField(name = "description")
    private final String description;

    @JSONField(name = "type")
    private final FieldType type;

    @JSONField(name = "required")
    private final boolean required;

    @JSONField(name = "defaultValue")
    private final String defaultValue;

    @JSONField(name = "pattern")
    private final String pattern;

    @JSONField(name = "constraints")
    private Map<String, Object> constraints;

    @JSONField(name = "enumValues")
    private List<String> enumValues;

    @JSONField(name = "properties")
    private Map<String, FieldDefinition> properties;

    @JSONField(name = "itemDefinition")
    private FieldDefinition itemDefinition;

    public FieldDefinition() {
        // Default constructor for Fastjson
        this.name = null;
        this.description = null;
        this.type = null;
        this.required = false;
        this.defaultValue = null;
        this.pattern = null;
    }

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

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public FieldType getType() { return type; }
    public boolean isRequired() { return required; }
    public String getDefaultValue() { return defaultValue; }
    public String getPattern() { return pattern; }
    public Map<String, Object> getConstraints() { return constraints; }
    public List<String> getEnumValues() { return enumValues; }
    public Map<String, FieldDefinition> getProperties() { return properties; }
    public FieldDefinition getItemDefinition() { return itemDefinition; }

    // Setters for mutable fields
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }
    public void setEnumValues(List<String> enumValues) { this.enumValues = enumValues; }
    public void setProperties(Map<String, FieldDefinition> properties) { this.properties = properties; }
    public void setItemDefinition(FieldDefinition itemDefinition) { this.itemDefinition = itemDefinition; }
} 
package cn.yafex.tools.schema;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

/**
 * Defines the structure and constraints of a field in a tool's input or output
 */
@JSONType(includes = {"name", "description", "type", "required", "defaultValue", "pattern", 
                     "constraints", "enumValues", "properties", "itemDefinition"})
public class FieldDefinition {
    @JSONField(name = "name", ordinal = 1)
    private String name;

    @JSONField(name = "description", ordinal = 2)
    private String description;

    @JSONField(name = "type", ordinal = 3)
    private FieldType type;

    @JSONField(name = "required", ordinal = 4)
    private boolean required;

    @JSONField(name = "defaultValue", ordinal = 5)
    private String defaultValue;

    @JSONField(name = "pattern", ordinal = 6)
    private String pattern;

    @JSONField(name = "constraints", ordinal = 7)
    private Map<String, Object> constraints;

    @JSONField(name = "enumValues", ordinal = 8)
    private List<String> enumValues;

    @JSONField(name = "properties", ordinal = 9)
    private Map<String, FieldDefinition> properties;

    @JSONField(name = "itemDefinition", ordinal = 10)
    private FieldDefinition itemDefinition;

    public FieldDefinition() {
        // Default constructor for Fastjson
        this.properties = new HashMap<>();
        this.enumValues = new ArrayList<>();
        this.constraints = new HashMap<>();
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
        this.properties = new HashMap<>();
        this.enumValues = new ArrayList<>();
        this.constraints = new HashMap<>();
    }

    // Copy constructor for deep cloning
    public FieldDefinition(FieldDefinition other) {
        this.name = other.name;
        this.description = other.description;
        this.type = other.type;
        this.required = other.required;
        this.defaultValue = other.defaultValue;
        this.pattern = other.pattern;
        this.constraints = other.constraints != null ? new HashMap<>(other.constraints) : new HashMap<>();
        this.enumValues = other.enumValues != null ? new ArrayList<>(other.enumValues) : new ArrayList<>();
        
        // Deep copy properties
        this.properties = new HashMap<>();
        if (other.properties != null) {
            for (Map.Entry<String, FieldDefinition> entry : other.properties.entrySet()) {
                this.properties.put(entry.getKey(), new FieldDefinition(entry.getValue()));
            }
        }
        
        // Deep copy itemDefinition
        this.itemDefinition = other.itemDefinition != null ? new FieldDefinition(other.itemDefinition) : null;
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

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { 
        this.constraints = constraints != null ? constraints : new HashMap<>(); 
    }

    public List<String> getEnumValues() { return enumValues; }
    public void setEnumValues(List<String> enumValues) { 
        this.enumValues = enumValues != null ? enumValues : new ArrayList<>(); 
    }

    public Map<String, FieldDefinition> getProperties() { return properties; }
    public void setProperties(Map<String, FieldDefinition> properties) { 
        this.properties = properties != null ? properties : new HashMap<>(); 
    }

    public FieldDefinition getItemDefinition() { return itemDefinition; }
    public void setItemDefinition(FieldDefinition itemDefinition) { this.itemDefinition = itemDefinition; }

    @Override
    public String toString() {
        return String.format("FieldDefinition{name='%s', description='%s', type=%s, required=%s}", 
            name, description, type, required);
    }
} 
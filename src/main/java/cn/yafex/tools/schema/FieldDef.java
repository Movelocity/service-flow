package cn.yafex.tools.schema;

import java.util.Map;
import java.util.HashMap;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;

/**
 * 定义工具输入或输出字段的结构和约束
 */
@JSONType(includes = {"name", "description", "type", "required", "defaultValue", "constraints", "properties", "itemDefinition"})
public class FieldDef {
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

    @JSONField(name = "constraints", ordinal = 6)
    private Map<String, Object> constraints;

    @JSONField(name = "properties", ordinal = 7)
    private Map<String, FieldDef> properties;

    @JSONField(name = "itemDefinition", ordinal = 8)
    private FieldDef itemDefinition;

    public FieldDef() {
        // Default constructor for Fastjson
        this.properties = new HashMap<>();
        this.constraints = new HashMap<>();
    }

    public FieldDef(
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
        this.properties = new HashMap<>();
        this.constraints = new HashMap<>();
    }

    // Copy constructor for deep cloning
    public FieldDef(FieldDef other) {
        this.name = other.name;
        this.description = other.description;
        this.type = other.type;
        this.required = other.required;
        this.defaultValue = other.defaultValue;
        this.constraints = other.constraints != null ? new HashMap<>(other.constraints) : new HashMap<>();
        // this.enumValues = other.enumValues != null ? new ArrayList<>(other.enumValues) : new ArrayList<>();
        
        // Deep copy properties
        this.properties = new HashMap<>();
        if (other.properties != null) {
            for (Map.Entry<String, FieldDef> entry : other.properties.entrySet()) {
                this.properties.put(entry.getKey(), new FieldDef(entry.getValue()));
            }
        }
        
        // Deep copy itemDefinition
        this.itemDefinition = other.itemDefinition != null ? new FieldDef(other.itemDefinition) : null;
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

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { 
        this.constraints = constraints != null ? constraints : new HashMap<>(); 
    }

    // public List<String> getEnumValues() { return enumValues; }
    // public void setEnumValues(List<String> enumValues) { 
    //     this.enumValues = enumValues != null ? enumValues : new ArrayList<>(); 
    // }

    public Map<String, FieldDef> getProperties() { return properties; }
    public void setProperties(Map<String, FieldDef> properties) { 
        this.properties = properties != null ? properties : new HashMap<>(); 
    }

    public FieldDef getItemDefinition() { return itemDefinition; }
    public void setItemDefinition(FieldDef itemDefinition) { this.itemDefinition = itemDefinition; }

    @Override
    public String toString() {
        return String.format("FieldDef{name='%s', description='%s', type=%s, required=%s}", 
            name, description, type, required);
    }
} 
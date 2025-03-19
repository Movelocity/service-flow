package cn.yafex.tools.schema;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 变量定义，用于描述变量的名称、类型、描述、默认值、父级等属性。
 * 适用于在节点间传递变量
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
        // 默认构造函数，便于 JSON 序列化
    }

    public VariableDefinition(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

	public VariableDefinition(String name, Object value) {
		this.name = name;
		this.value = value;
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
        return String.format("变量定义{name:'%s', type:'%s', description:'%s', value:%s, defaultValue:%s, parent:%s}", 
            name, type, description, value, defaultValue, parent);
    }

	public static VariableDefinition fromFieldDefinition(FieldDefinition fieldDefinition, String parent) {
		VariableDefinition varDef = new VariableDefinition();
		varDef.name = fieldDefinition.getName();
		varDef.type = fieldDefinition.getType().toString();
		varDef.description = fieldDefinition.getDescription();
		varDef.defaultValue = fieldDefinition.getDefaultValue();
		varDef.parent = parent;
		return varDef;
	}
} 
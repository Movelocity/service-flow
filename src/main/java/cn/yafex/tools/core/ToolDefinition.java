package cn.yafex.tools.core;

import cn.yafex.tools.schema.FieldDefinition;
import cn.yafex.tools.exceptions.ToolException;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * Defines a tool's properties, input parameters, and output format
 */
public class ToolDefinition {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "description")
    private String description;

    @JSONField(name = "inputFields")
    private Map<String, FieldDefinition> inputFields;

    @JSONField(name = "outputFields")
    private Map<String, FieldDefinition> outputFields;

    @JSONField(name = "possibleExceptions")
    private List<Class<? extends ToolException>> possibleExceptions;

    public ToolDefinition() {
        // Default constructor for Fastjson
    }

    public ToolDefinition(String name, String description,
                         Map<String, FieldDefinition> inputFields,
                         Map<String, FieldDefinition> outputFields,
                         List<Class<? extends ToolException>> possibleExceptions) {
        this.name = name;
        this.description = description;
        this.inputFields = inputFields;
        this.outputFields = outputFields;
        this.possibleExceptions = possibleExceptions;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, FieldDefinition> getInputFields() { return inputFields; }
    public void setInputFields(Map<String, FieldDefinition> inputFields) { this.inputFields = inputFields; }

    public Map<String, FieldDefinition> getOutputFields() { return outputFields; }
    public void setOutputFields(Map<String, FieldDefinition> outputFields) { this.outputFields = outputFields; }

    public List<Class<? extends ToolException>> getPossibleExceptions() { return possibleExceptions; }
    public void setPossibleExceptions(List<Class<? extends ToolException>> possibleExceptions) {
        this.possibleExceptions = possibleExceptions;
    }
} 
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

    @JSONField(name = "inputs")
    private Map<String, FieldDefinition> inputs;

    @JSONField(name = "outputs")
    private Map<String, FieldDefinition> outputs;

    @JSONField(name = "possibleExceptions")
    private List<Class<? extends ToolException>> possibleExceptions;

    public ToolDefinition() {
        // Default constructor for Fastjson
    }

    public ToolDefinition(String name, String description,
                         Map<String, FieldDefinition> inputs,
                         Map<String, FieldDefinition> outputs,
                         List<Class<? extends ToolException>> possibleExceptions) {
        this.name = name;
        this.description = description;
        this.inputs = inputs;
        this.outputs = outputs;
        this.possibleExceptions = possibleExceptions;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, FieldDefinition> getInputs() { return inputs; }
    public void setInputs(Map<String, FieldDefinition> inputs) { this.inputs = inputs; }

    public Map<String, FieldDefinition> getOutputs() { return outputs; }
    public void setOutputs(Map<String, FieldDefinition> outputs) { this.outputs = outputs; }

    public List<Class<? extends ToolException>> getPossibleExceptions() { return possibleExceptions; }
    public void setPossibleExceptions(List<Class<? extends ToolException>> possibleExceptions) {
        this.possibleExceptions = possibleExceptions;
    }
} 
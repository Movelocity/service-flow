package cn.yafex.tools.core;

import cn.yafex.tools.schema.FieldDef;
import cn.yafex.tools.exceptions.ToolException;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Map;

/**
 * 定义工具的属性、输入参数和输出格式
 */
public class ToolDefinition {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "description")
    private String description;

    @JSONField(name = "inputs")
    private Map<String, FieldDef> inputs;

    @JSONField(name = "outputs")
    private Map<String, FieldDef> outputs;

    @JSONField(name = "possibleExceptions")
    private List<Class<? extends ToolException>> possibleExceptions;

    public ToolDefinition() {
        // Fastjson 默认构造函数
    }

    public ToolDefinition(String name, String description,
                         Map<String, FieldDef> inputs,
                         Map<String, FieldDef> outputs,
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

    public Map<String, FieldDef> getInputs() { return inputs; }
    public void setInputs(Map<String, FieldDef> inputs) { this.inputs = inputs; }

    public Map<String, FieldDef> getOutputs() { return outputs; }
    public void setOutputs(Map<String, FieldDef> outputs) { this.outputs = outputs; }

    public List<Class<? extends ToolException>> getPossibleExceptions() { return possibleExceptions; }
    public void setPossibleExceptions(List<Class<? extends ToolException>> possibleExceptions) {
        this.possibleExceptions = possibleExceptions;
    }
} 
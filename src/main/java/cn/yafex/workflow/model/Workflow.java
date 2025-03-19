package cn.yafex.workflow.model;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.schema.VariableDef;
import cn.yafex.tools.schema.FieldDef;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 工作流定义类
 */
public class Workflow {
	/** Unique ID */
    private String id;
    private String name;
    private String description;
	/** 工作流输入参数 */
    private Map<String, FieldDef> inputs;  // Workflow level inputs
	/** 工作流输出参数 */
    private Map<String, VariableDef> outputs; // Workflow level outputs
	/** 工作流内保存的工具名称和输入输出模式，用于判断工具是否发生不兼容的更新 */
    private Map<String, ToolDefinition> tools; // Map of tool name to tool definition
	/** 节点，用于描述工作流的执行流程 */
    private List<WorkflowNode> nodes;
	/** 开始节点ID，用于描述工作流的执行流程 */
    private String startNodeId;
	/** 工作流是否激活 */
    private boolean isActive;

    public Workflow() {
        this.inputs = new HashMap<>();
        this.outputs = new HashMap<>();
        this.tools = new HashMap<>();
        this.nodes = new ArrayList<>();
        this.isActive = true;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, FieldDef> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, FieldDef> inputs) {
        this.inputs = inputs;
    }

    public Map<String, VariableDef> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, VariableDef> outputs) {
        this.outputs = outputs;
    }

    public Map<String, ToolDefinition> getTools() {
        return tools;
    }

    public void setTools(Map<String, ToolDefinition> tools) {
        this.tools = tools;
    }

    public List<WorkflowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<WorkflowNode> nodes) {
        this.nodes = nodes;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Add a node to the workflow
     * @param node The node to add
     */
    public void addNode(WorkflowNode node) {
        this.nodes.add(node);
    }

    /**
     * Get a node by its ID
     * @param nodeId The ID of the node to find
     * @return The node if found, null otherwise
     */
    public WorkflowNode getNodeById(String nodeId) {
        return this.nodes.stream()
                .filter(node -> node.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add a tool definition to the workflow
     * @param tool The tool definition to add
     * @throws IllegalArgumentException if a tool with the same name already exists
     */
    public void addTool(ToolDefinition tool) {
        if (tool != null) {
            String toolName = tool.getName();
            if (tools.containsKey(toolName)) {
                throw new IllegalArgumentException("工具已存在于工作流中: " + toolName);
            }
            tools.put(toolName, tool);
        }
    }

    /**
     * Add or update a tool definition in the workflow
     * @param tool The tool definition to add or update
     */
    public void setTool(ToolDefinition tool) {
        if (tool != null) {
            tools.put(tool.getName(), tool);
        }
    }

    /**
     * Get a tool definition by name
     * @param toolName The name of the tool to find
     * @return The tool definition if found, null otherwise
     */
    public ToolDefinition getToolDefinition(String toolName) {
        return this.tools.get(toolName);
    }

    /**
     * Check if a tool is available in this workflow
     * @param toolName The name of the tool to check
     * @return true if the tool is available, false otherwise
     */
    public boolean hasTool(String toolName) {
        return this.tools.containsKey(toolName);
    }

    /**
     * Get all tool names used in this workflow
     * @return List of tool names
     */
    public List<String> getToolNames() {
        return new ArrayList<>(this.tools.keySet());
    }
} 
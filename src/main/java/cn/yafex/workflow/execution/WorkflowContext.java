package cn.yafex.workflow.execution;

import cn.yafex.tools.schema.VariableDefinition;
import cn.yafex.workflow.model.Workflow;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Class representing the execution context of a workflow
 */
public class WorkflowContext {
    private String executionId;
    private String workflowId;
    private Workflow workflow;
    private Map<String, VariableDefinition> variables;
    private String currentNodeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WorkflowStatus status;

    public WorkflowContext(String workflowId) {
        this.executionId = UUID.randomUUID().toString();
        this.workflowId = workflowId;
        this.variables = new HashMap<>();
        this.startTime = LocalDateTime.now();
        this.status = WorkflowStatus.RUNNING;
    }

    public WorkflowContext(Workflow workflow) {
        this(workflow.getId());
        this.workflow = workflow;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

	/** 
	 * 获取工作流执行上下文变量。
	 * 
	 * 此方法返回一个可以在工作流执行期间使用的变量映射。
	 * 
	 * 使用示例：
	 * 
	 *     result = executeTool(node.getToolName(), toolInputs);
	 *     context.getVariables().putAll(result);
	 * 
	 * @return 一个包含工作流执行上下文变量的映射。
	 */
    public Map<String, VariableDefinition> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, VariableDefinition> variables) {
        this.variables = variables;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Set a variable in the context
     * @param key Variable name
     * @param value Variable value
     */
    public void setVariable(String key, VariableDefinition value) {
        this.variables.put(key, value);
    }

    /**
     * 获取保存在工作流 context 中的变量，如果变量不存在，则返回 null。
     * 
     * @param key 变量名称
     * @return 变量值
     */
    public VariableDefinition getVariable(String key) {
        return this.variables.get(key);
    }

	/**
	 * 获取保存在工作流 context 中的变量，如果变量不存在，则返回 null。
	 * 
	 * @param name 变量名称
	 * @param parent 变量父级名称
	 * @return 变量值
	 */
	public VariableDefinition getVariable(String name, String parent) {
		for (VariableDefinition variable : this.variables.values()) {
			if (variable.getName().equals(name) && variable.getParent().equals(parent)) {
				return variable;
			}
		}
		return null;
	}
} 
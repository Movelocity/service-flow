package cn.yafex.workflow.model;

import java.util.Map;
import java.util.HashMap;

import cn.yafex.tools.schema.VariableDef;

/** 节点执行结果，包含节点类型、输出结果、异常信息、条件节点对应输出、
 * 有多种构造方法，根据节点类型和执行结果构造不同的节点执行结果，请根据实际情况选择使用
 * @isEvaluated()：条件节点是否执行成功
 * @hasError()：是否存在异常
 */
public class NodeResult {
	private NodeType nodeType;
	private Map<String, VariableDef> outputs;

	/** 异常输出信息 */
	private String errorCode;
	private String errorMessage;
	private String errorDetails;

	/** 条件节点对应输出 */
	private String matchedCase;
	private boolean evaluated;
	
	/** 函数节点执行结果 */
	public NodeResult(NodeType nodeType, Map<String, VariableDef> outputs) {
		this.nodeType = nodeType;
		this.outputs = outputs;
	}

	/** 条件节点执行结果 */
	public NodeResult(NodeType nodeType, String matchedCase, boolean evaluated) {
		this.nodeType = nodeType;
		this.matchedCase = matchedCase;
		this.evaluated = evaluated;

		this.outputs = new HashMap<>();
		this.outputs.put("matchedCase", new VariableDef("matchedCase", matchedCase));
		this.outputs.put("evaluated", new VariableDef("evaluated", evaluated));
	}

	/** 节点执行异常 */
	public NodeResult(NodeType nodeType, String errorCode, String errorMessage, String errorDetails) {
		this.nodeType = nodeType;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.errorDetails = errorDetails;
	}

	/** 创建节点执行异常信息 */
	public static NodeResult error(NodeType nodeType, String errorCode, String errorMessage, String errorDetails) {
		return new NodeResult(nodeType, errorCode, errorMessage, errorDetails);
	}

	/** 开始或结束节点 */
	public NodeResult(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	/** 获取节点执行结果 */
	public Map<String, VariableDef> getOutputs() {
		return outputs;
	}

	/** 是否存在异常 */
	public boolean hasError() {
		return errorCode != null;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	/** 获取条件节点对应输出 */
	public String getMatchedCase() {
		return matchedCase;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public boolean isEvaluated() {
		return evaluated;
	}
}
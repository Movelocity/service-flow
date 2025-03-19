package cn.yafex.workflow.service;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.schema.FieldDef;
import cn.yafex.tools.schema.FieldType;
import cn.yafex.tools.schema.VariableDef;
import cn.yafex.workflow.model.*;

import java.util.*;
import org.springframework.stereotype.Service;

/**
 * 检查工作流配置中可能存在的问题
 * 包括：
 * 1. 缺失必填参数
 * 2. 参数类型不匹配
 * 3. 主干的条件分支存在未连接的输出
 */
@Service
public class WorkflowChecker {
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        
        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        @Override
        public String toString() {
            if (valid) {
                return "Workflow is valid.";
            } else {
                return "Workflow validation failed:\n" + String.join("\n", errors);
            }
        }
    }
    
    /**
     * 从指定节点开始验证工作流
     * @param workflow 要验证的工作流
     * @param startNodeId 可选的开始节点ID（如果为null，使用工作流的默认开始节点）
     * @return 包含任何错误的验证结果
     */
    public ValidationResult validateWorkflow(Workflow workflow, String startNodeId) {
        ValidationResult result = new ValidationResult();
        
        if (workflow == null) {
            result.addError("Workflow cannot be null");
            return result;
        }
        
        // 如果未指定startNodeId，使用工作流的默认开始节点
        if (startNodeId == null || startNodeId.isEmpty()) {
            startNodeId = workflow.getStartNodeId();
        }
        
        // 检查开始节点是否存在
        WorkflowNode startNode = workflow.getNodeById(startNodeId);
        if (startNode == null) {
            result.addError("Start node not found: " + startNodeId);
            return result;
        }
        
        // 跟踪已访问节点以避免循环
        Set<String> visitedNodes = new HashSet<>();
        
        // 从开始节点开始遍历
        validateNode(workflow, startNode, visitedNodes, result);
        
        return result;
    }
    
    /**
     * 递归验证节点及其子节点
     */
    private void validateNode(Workflow workflow, WorkflowNode node, Set<String> visitedNodes, ValidationResult result) {
        if (node == null || visitedNodes.contains(node.getId())) {
            return; // 跳过空节点或已访问节点
        }
        
        visitedNodes.add(node.getId());
        
        switch (node.getType()) {
            case FUNCTION:
                validateFunctionNode(workflow, node, result);
                break;
            case CONDITION:
                validateConditionNode(workflow, node, result);
                break;
            case START:
            case END:
                // 不需要对START/END节点进行特殊验证
                break;
        }
        
        // 继续验证下一个节点（除了END节点，它们没有下一个节点）
        if (node.getType() != NodeType.END) {
            validateNextNodes(workflow, node, visitedNodes, result);
        }
    }
    
    /**
     * 验证函数节点的参数
     */
    private void validateFunctionNode(Workflow workflow, WorkflowNode node, ValidationResult result) {
        String toolName = node.getToolName();
        if (toolName == null || toolName.isEmpty()) {
            result.addError("Function node '" + node.getName() + "' (ID: " + node.getId() + ") has no tool name specified");
            return;
        }
        
        ToolDefinition toolDef = workflow.getToolDefinition(toolName);
        if (toolDef == null) {
            result.addError("Function node '" + node.getName() + "' (ID: " + node.getId() + 
                    ") references unknown tool: " + toolName);
            return;
        }
        
        // 检查必填参数
        Map<String, FieldDef> requiredParams = toolDef.getInputs();
        if (requiredParams != null) {
            for (Map.Entry<String, FieldDef> entry : requiredParams.entrySet()) {
                String paramName = entry.getKey();
                FieldDef paramDef = entry.getValue();
                
                // 跳过非必填参数
                if (!paramDef.isRequired()) {
                    continue;
                }
                
                VariableDef varDef = node.getInputMap().get(paramName);
                if (varDef == null) {
                    result.addError("Function node '" + node.getName() + "' (ID: " + node.getId() + 
                            ") is missing required parameter: " + paramName);
                    continue;
                }
                
                // 检查类型兼容性
                if (varDef.getValue() != null || varDef.getDefaultValue() != null) {
                    if (!isTypeCompatible(paramDef.getType(), varDef.getType())) {
                        result.addError("Function node '" + node.getName() + "' (ID: " + node.getId() + 
                                ") parameter '" + paramName + "' has incompatible type. Expected: " + 
                                paramDef.getType() + ", Found: " + varDef.getType());
                    }
                } else if (varDef.getParent() != null && !varDef.getParent().isEmpty()) {
                    // 如果存在父引用，确保父节点存在并且输出兼容的值
                    // 这可能更复杂，需要跟踪执行上下文
                    // 为简单起见，我们只检查父节点是否存在
                    String parentNodeId = varDef.getParent();
                    if (!"global".equals(parentNodeId)) { // 跳过全局变量
                        WorkflowNode parentNode = workflow.getNodeById(parentNodeId);
                        if (parentNode == null) {
                            result.addError("Function node '" + node.getName() + "' (ID: " + node.getId() + 
                                    ") parameter '" + paramName + "' references non-existent parent node: " + parentNodeId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Validate a condition node's variables and branches
     */
    private void validateConditionNode(Workflow workflow, WorkflowNode node, ValidationResult result) {
        List<ConditionCase> conditionCases = node.getConditions();
        if (conditionCases == null || conditionCases.isEmpty()) {
            result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + ") has no conditions");
            return;
        }
        
        // Validate variables in each condition
        // for (ConditionCase conditionCase : conditionCases) {
        //     List<Condition> conditions = conditionCase.getConditions();
        //     if (conditions != null) {
        //         for (Condition condition : conditions) {
        //             validateConditionVariable(workflow, node, condition.getLeftOperand(), "left operand", result);
        //             validateConditionVariable(workflow, node, condition.getRightOperand(), "right operand", result);
                    
        //             // Check if operands' types are compatible with each other
        //             if (condition.getLeftOperand() != null && condition.getRightOperand() != null) {
        //                 if (!isTypeCompatible(condition.getLeftOperand().getType(), condition.getRightOperand().getType())) {
        //                     result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + 
        //                             ") has incompatible operand types in condition: " + condition.getLeftOperand().getType() + 
        //                             " " + condition.getOperator() + " " + condition.getRightOperand().getType());
        //                 }
        //             }
        //         }
        //     }
        // }
        
        // Check for missing case branches
        Map<String, String> nextNodes = node.getNextNodes();
        if (nextNodes == null || nextNodes.isEmpty()) {
            result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + ") has no next nodes");
            return;
        }
        
        // Expected nextNodes should include all cases from the conditions plus 'else'
        Set<String> expectedCases = new HashSet<>();
        for (int i = 0; i < conditionCases.size(); i++) {
            expectedCases.add("case" + (i + 1));
        }
        expectedCases.add("else");
        
        // Check for missing branches
        for (String caseKey : expectedCases) {
            if (!nextNodes.containsKey(caseKey)) {
                result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + 
                        ") is missing branch for: " + caseKey);
            }
        }
    }
    
    /**
     * Validate a variable reference in a condition
     */
    // private void validateConditionVariable(Workflow workflow, WorkflowNode node, VariableDef varDef, String operandType, ValidationResult result) {
    //     if (varDef == null) {
    //         result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + 
    //                 ") has null " + operandType);
    //         return;
    //     }
        
    //     if ("CONSTANT".equals(varDef.getType())) {
    //         // Constants are directly in the condition, no need to check references
    //         return;
    //     }
        
    //     // Check if variable reference exists
    //     String parentNodeId = varDef.getParent();
    //     if (parentNodeId == null || parentNodeId.isEmpty()) {
    //         result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + 
    //                 ") has " + operandType + " with no parent reference: " + varDef.getName());
    //         return;
    //     }
        
    //     if (!"global".equals(parentNodeId)) {
    //         WorkflowNode parentNode = workflow.getNodeById(parentNodeId);
    //         if (parentNode == null) {
    //             result.addError("Condition node '" + node.getName() + "' (ID: " + node.getId() + 
    //                     ") references non-existent parent node in " + operandType + ": " + parentNodeId);
    //         }
    //     }
    // }
    
    /**
     * Validate all next nodes from a given node
     */
    private void validateNextNodes(Workflow workflow, WorkflowNode node, Set<String> visitedNodes, ValidationResult result) {
        Map<String, String> nextNodes = node.getNextNodes();
        if (nextNodes == null || nextNodes.isEmpty()) {
            if (node.getType() != NodeType.END) {
                result.addError("Node '" + node.getName() + "' (ID: " + node.getId() + 
                        ") has no next nodes but is not an END node");
            }
            return;
        }
        
        for (Map.Entry<String, String> entry : nextNodes.entrySet()) {
            String branch = entry.getKey();
            String nextNodeId = entry.getValue();
            
            if (nextNodeId == null || nextNodeId.isEmpty()) {
                result.addError("Node '" + node.getName() + "' (ID: " + node.getId() + ") has empty next node ID for branch: " + branch);
                continue;
            }
            
            WorkflowNode nextNode = workflow.getNodeById(nextNodeId);
            if (nextNode == null) {
                result.addError("Node '" + node.getName() + "' (ID: " + node.getId() + 
                        ") references non-existent next node: " + nextNodeId + " (branch: " + branch + ")");
                continue;
            }
            
            // Recursively validate next node
            validateNode(workflow, nextNode, visitedNodes, result);
        }
    }
    
    /**
     * Check if two types are compatible (FieldType, String)
     */
    private boolean isTypeCompatible(FieldType type1, String type2) {
        if (type1 == null || type2 == null) {
            return false;
        }
        
        // If they're the same type, they're compatible
        if (type1.name().equalsIgnoreCase(type2)) {
            return true;
        }
        
        // Handle numeric types
        boolean isType1Numeric = type1 == FieldType.NUMBER;
        boolean isType2Numeric = "NUMBER".equalsIgnoreCase(type2) || "INTEGER".equalsIgnoreCase(type2);
        
        return isType1Numeric && isType2Numeric;
    }
    
    /**
     * Check if two types are compatible (String, String)
     */
    // private boolean isTypeCompatible(String type1, String type2) {
    //     if (type1 == null || type2 == null) {
    //         return false;
    //     }
        
    //     // If they're the same type, they're compatible
    //     if (type1.equalsIgnoreCase(type2)) {
    //         return true;
    //     }
        
    //     // Handle numeric types
    //     boolean isType1Numeric = "NUMBER".equalsIgnoreCase(type1) || "INTEGER".equalsIgnoreCase(type1);
    //     boolean isType2Numeric = "NUMBER".equalsIgnoreCase(type2) || "INTEGER".equalsIgnoreCase(type2);
        
    //     return isType1Numeric && isType2Numeric;
    // }
} 
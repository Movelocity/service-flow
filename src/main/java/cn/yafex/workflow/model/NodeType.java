package cn.yafex.workflow.model;

/**
 * Enum representing different types of nodes in a workflow
 */
public enum NodeType {
    START,      // Starting node of the workflow
    CONDITION,  // Node for conditional branching
    FUNCTION,   // Node for function execution
    END        // End node of the workflow
} 
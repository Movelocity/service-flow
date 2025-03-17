package cn.yafex.workflow.model;

/**
 * 工作流节点类型枚举
 */
public enum NodeType {
    START,      // 工作流开始节点
    CONDITION,  // 条件分支节点
    FUNCTION,   // 函数执行节点
    END        // 工作流结束节点
} 
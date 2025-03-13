

/** 节点执行事件 */
export interface NodeExecutionEvent {
  executionId: string; // 工作流执行ID
  nodeId: string; // 节点ID
  nodeName: string; // 节点名称
  nodeType: string; // 节点类型
  eventType: 'ENTER' | 'COMPLETE'; // 事件类型
  contextVariables?: Record<string, any>; // 上文变量
  nodeResult?: Record<string, any>; // 节点结果
  timestamp: string; // 时间戳
  duration?: number; // 执行时长
}
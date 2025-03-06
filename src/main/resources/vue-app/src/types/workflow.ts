/**
 * 工作流节点类型枚举
 */
export enum NodeType {
  START = 'START',
  FUNCTION = 'FUNCTION',
  CONDITION = 'CONDITION',
  END = 'END'
}

/**
 * 位置接口
 */
export interface Position {
  x: number;
  y: number;
}

/**
 * API响应中的节点接口
 */
export interface ApiNode {
  id: string;
  type: NodeType;
  name: string;
  position: Position;
  parameters: Record<string, any>;
  nextNodes: {
    [key: string]: string; // key可以是'default'/'true'/'false', value是目标节点id
  };
}

/**
 * API响应中的工作流接口
 */
export interface ApiWorkflow {
  id: string;
  name: string;
  description: string;
  nodes: ApiNode[];
  startNodeId: string;
  active: boolean;
  globalVariables?: Record<string, any>;
}

/**
 * 节点接口
 */
export interface Node {
  id: string;
  type: NodeType;
  name: string;
  position: Position;
  parameters: Record<string, any>;
  nextNodes: {
    [key: string]: string; // key可以是'default'/'true'/'false', value是目标节点id
  };
}

/**
 * 工作流接口
 */
export interface Workflow {
  id: string;
  name: string;
  description: string;
  nodes: Node[];
  createdAt?: Date;
  updatedAt?: Date;
}

/**
 * 画布状态接口
 */
export interface CanvasState {
  scale: number;
  position: Position;
  isDragging: boolean;
}

/**
 * 编辑器状态接口
 */
export interface EditorState {
  selectedNodeId: string | null;
  selectedCondition: string | null; // 用于标识选中的连接条件
  isEditorPanelOpen: boolean;
  canvasState: CanvasState;
}

/**
 * 将API工作流格式转换为Vue应用工作流格式
 */
export function convertApiToAppWorkflow(apiWorkflow: ApiWorkflow): Workflow {
  return {
    id: apiWorkflow.id,
    name: apiWorkflow.name,
    description: apiWorkflow.description,
    nodes: apiWorkflow.nodes
  };
}

/**
 * 将Vue应用工作流格式转换为API工作流格式
 */
export function convertAppToApiWorkflow(workflow: Workflow): ApiWorkflow {
  return {
    id: workflow.id,
    name: workflow.name,
    description: workflow.description,
    nodes: workflow.nodes,
    startNodeId: workflow.nodes.find(n => n.type === NodeType.START)?.id || '',
    active: true
  };
} 
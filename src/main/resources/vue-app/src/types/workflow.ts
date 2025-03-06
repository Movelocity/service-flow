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
 * 节点接口
 */
export interface Node {
  id: string;
  type: NodeType;
  name: string;
  position: Position;
  parameters: Record<string, any>;
}

/**
 * 连接接口
 */
export interface Connection {
  id: string;
  sourceNodeId: string;
  targetNodeId: string;
  condition?: string; // 用于条件节点的分支条件
}

/**
 * 工作流接口
 */
export interface Workflow {
  id: string;
  name: string;
  description: string;
  nodes: Node[];
  connections: Connection[];
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
  isEditorPanelOpen: boolean;
  canvasState: CanvasState;
} 
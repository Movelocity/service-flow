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
  nextNodes?: Record<string, string>;
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

/**
 * 将API工作流格式转换为Vue应用工作流格式
 */
export function convertApiToAppWorkflow(apiWorkflow: ApiWorkflow): Workflow {
  const nodes = apiWorkflow.nodes.map(({ nextNodes, ...node }) => node);
  const connections: Connection[] = [];

  apiWorkflow.nodes.forEach(node => {
    Object.entries(node.nextNodes).forEach(([condition, targetNodeId]) => {
      connections.push({
        id: `${node.id}-${targetNodeId}`,
        sourceNodeId: node.id,
        targetNodeId,
        condition: condition === 'default' ? undefined : condition
      });
    });
  });

  return {
    id: apiWorkflow.id,
    name: apiWorkflow.name,
    description: apiWorkflow.description,
    nodes,
    connections
  };
}

/**
 * 将Vue应用工作流格式转换为API工作流格式
 */
export function convertAppToApiWorkflow(workflow: Workflow): ApiWorkflow {
  const nodes: ApiNode[] = workflow.nodes.map(node => ({
    ...node,
    nextNodes: {}
  }));

  // 构建nextNodes映射
  workflow.connections.forEach(connection => {
    const sourceNode = nodes.find(n => n.id === connection.sourceNodeId);
    if (sourceNode) {
      const condition = connection.condition || 'default';
      sourceNode.nextNodes[condition] = connection.targetNodeId;
    }
  });

  return {
    id: workflow.id,
    name: workflow.name,
    description: workflow.description,
    nodes,
    startNodeId: workflow.nodes.find(n => n.type === NodeType.START)?.id || '',
    active: true
  };
} 
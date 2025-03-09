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
 * 变量类型枚举
 */
export enum VariableType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  BOOLEAN = 'BOOLEAN',
  OBJECT = 'OBJECT',
  ARRAY = 'ARRAY',
  DATE = 'DATE',
}

/**
 * 变量定义接口
 */
export interface VariableDefinition {
  name: string;
  type: VariableType;
  description?: string;
  required: boolean;
  defaultValue?: any;
}

/**
 * 工作流变量接口
 */
export interface WorkflowVariable {
  parent: string;  // 变量来源节点的ID
  name: string;    // 变量名称
  type: VariableType;
  value: any;
}

/**
 * 工具字段定义接口
 */
export interface ToolField {
  name: string;
  description: string;
  type: VariableType;
  required: boolean;
  defaultValue?: any;
  constraints?: any;
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
  description: string;
  position: Position;
  nextNodes: {
    [key: string]: string; // key可以是'default'/'true'/'false', value是目标节点id
  };
  inputs: Record<string, VariableDefinition>;
  outputs: Record<string, VariableDefinition>;
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
  description: string;
  position: Position;
  // parameters: Record<string, any>;
  nextNodes: {
    [key: string]: string; // key可以是'default'/'true'/'false', value是目标节点id
  };
  inputs: Record<string, VariableDefinition>;  // 节点接收的输入变量定义
  outputs: Record<string, VariableDefinition>; // 节点产生的输出变量定义
  toolName?: string;              // 函数节点的工具定义
  toolDescription?: string;
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
  globalVariables?: VariableDefinition[];  // 工作流级别的全局变量定义
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
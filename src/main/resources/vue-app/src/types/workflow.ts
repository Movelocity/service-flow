import type { VariableType, VariableDefinition, ConditionCase } from '@/types/fields';
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
 * 工具定义接口
 */
export interface ToolDefinition {
  name: string;
  description: string;
  inputs: { [key: string]: VariableDefinition };
  outputs: { [key: string]: VariableDefinition };
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
    [key: string]: string;
  };
  toolName?: string;
}

/**
 * API响应中的工作流接口
 */
export interface ApiWorkflow {
  id: string;
  name: string;
  description: string;
  inputs: { [key: string]: Omit<VariableDefinition, 'name'> };
  outputs: { [key: string]: Omit<VariableDefinition, 'name'> };
  tools: { [name: string]: Omit<ToolDefinition, 'name'> };
  nodes: ApiNode[];
  startNodeId: string;
  isActive: boolean;
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
  nextNodes: {
    [key: string]: string; // key可以是'default'/'true'/'false', value是目标节点id
  };
  toolName?: string;              // 仅用于 FUNCTION 类型节点
  context?:VariableDefinition[]; // 新增：存储函数节点的输出
  conditions?: ConditionCase[];
  inputMap?: Record<string, VariableDefinition>;
}

/**
 * 工作流接口
 */
export interface Workflow {
  id: string;
  name: string;
  description: string;
  inputs: VariableDefinition[];  // 新增：工作流级别输入
  outputs: VariableDefinition[]; // 新增：工作流级别输出
  tools: ToolDefinition[];      // 新增：工具定义移至工作流级别
  nodes: Node[];
  startNodeId: string;
  isActive: boolean;
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
  // Convert map to array for inputs
  const inputs = Object.entries(apiWorkflow.inputs).map(([name, def]) => ({
    name,
    ...def
  }));

  // Convert map to array for outputs
  const outputs = Object.entries(apiWorkflow.outputs).map(([name, def]) => ({
    name,
    ...def
  }));

  // Convert map to array for tools
  const tools = Object.entries(apiWorkflow.tools).map(([name, def]) => ({
    name,
    ...def
  }));

  const nodes = apiWorkflow.nodes.map(node => {
    // drop context
    const newNode = {
      ...node,
      context: []
    }
    return newNode
  })

  return {
    id: apiWorkflow.id,
    name: apiWorkflow.name,
    description: apiWorkflow.description,
    inputs,
    outputs,
    tools,
    nodes,
    startNodeId: apiWorkflow.startNodeId,
    isActive: apiWorkflow.isActive
  };
}

/**
 * 将Vue应用工作流格式转换为API工作流格式
 */
export function convertAppToApiWorkflow(workflow: Workflow): ApiWorkflow {
  // Convert array to map for inputs
  const inputs: { [key: string]: Omit<VariableDefinition, 'name'> } = {};
  workflow.inputs.forEach(input => {
    const { name } = input;
    inputs[name] = input;
  });

  // Convert array to map for outputs
  const outputs: { [key: string]: Omit<VariableDefinition, 'name'> } = {};
  workflow.outputs.forEach(output => {
    const { name, ...def } = output;
    outputs[name] = def;
  });

  // Convert array to map for tools
  const tools: { [name: string]: Omit<ToolDefinition, 'name'> } = {};
  workflow.tools.forEach(tool => {
    const { name } = tool;
    tools[name] = tool;
  });

  const nodes: ApiNode[] = workflow.nodes.map(node => {
    const { context, ...rest } = node
    return rest
  });

  return {
    id: workflow.id,
    name: workflow.name,
    description: workflow.description,
    inputs,
    outputs,
    tools,
    nodes,
    startNodeId: workflow.startNodeId,
    isActive: workflow.isActive
  };
} 
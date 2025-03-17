import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { Node, Workflow, Position, EditorState } from '@/types/workflow';
import type { VariableDefinition } from '@/types/fields';
import type { Tool } from '@/types/tools';
import { convertApiToAppWorkflow, convertAppToApiWorkflow, NodeType } from '@/types/workflow';
import { WorkflowApi } from '@/services/workflowApi';
import { VariableType } from '@/types/fields';
const workflowApi = new WorkflowApi();

interface WorkflowState {
  currentWorkflow: Workflow | null;
  editorState: EditorState;
  history: {
    past: Workflow[];
    future: Workflow[];
  };
  isDirty: boolean;
}

const defaultEditorState: EditorState = {
  selectedNodeId: null,
  selectedCondition: null,
  isEditorPanelOpen: false,
  canvasState: {
    scale: 1,
    position: { x: 0, y: 0 },
    isDragging: false
  }
};

export const useWorkflowStore = defineStore('workflow', {
  state: (): WorkflowState => ({
    currentWorkflow: null,
    editorState: { ...defaultEditorState },
    history: {
      past: [],
      future: []
    },
    isDirty: false
  }),

  getters: {
    selectedNode: (state): Node | null => {
      if (!state.currentWorkflow || !state.editorState.selectedNodeId) return null;
      return state.currentWorkflow.nodes.find(node => node.id === state.editorState.selectedNodeId) || null;
    },

    // 获取节点的所有输入连接
    nodeInputConnections: (state) => (nodeId: string): Array<{ sourceId: string; condition: string }> => {
      if (!state.currentWorkflow?.nodes) return [];
      const inboundNodes = state.currentWorkflow.nodes
        .filter(node => Object.entries(node.nextNodes)
          .some(([_condition, targetId]) => targetId === nodeId))
        .map(node => {
          const condition = Object.entries(node.nextNodes)
            .find(([_, targetId]) => targetId === nodeId)?.[0] || 'default';
          return { sourceId: node.id, condition };
        });
      return inboundNodes;
    },

    // 获取节点的所有输出连接
    nodeOutputConnections: (state) => (nodeId: string): Array<{ targetId: string; condition: string }> => {
      if (!state.currentWorkflow?.nodes) return [];
      const node = state.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (!node) return [];
      const outboundNodes = Object.entries(node.nextNodes)
        .map(([condition, targetId]) => ({ targetId, condition }));
      return outboundNodes;
    },

    // 获取节点的所有前置节点ID
    getNodePredecessors: (state) => (nodeId: string): string[] => {
      if (!state.currentWorkflow?.nodes) return [];
      return state.currentWorkflow.nodes
        .filter(node => Object.values(node.nextNodes).includes(nodeId))
        .map(node => node.id);
    }
  },

  actions: {
    // 工作流基本操作
    createWorkflow(name: string, description: string) {
      this.currentWorkflow = {
        id: uuidv4(),
        name,
        description,
        inputs: [],
        outputs: [],
        tools: [],
        nodes: [],
        startNodeId: '',
        isActive: true
      };
    },

    async loadWorkflow(id: string) {
      try {
        const workflow = await workflowApi.getWorkflow(id);
        this.currentWorkflow = convertApiToAppWorkflow(workflow);
        this.initializeWorkflowContext(); // Initialize context after loading
      } catch (error) {
        console.error('Error loading workflow:', error);
        throw error;
      }
    },

    updateWorkflow(workflow: Workflow) {
      this.currentWorkflow = workflow;
      this.editorState = { ...defaultEditorState };
      this.history.past = [];
      this.history.future = [];
      this.isDirty = false;
      this.ensureStartNode();
    },

    // 确保工作流有一个开始节点
    ensureStartNode() {
      if (!this.currentWorkflow) return;
      
      const hasStartNode = this.currentWorkflow.nodes.some(node => node.type === NodeType.START);
      if (!hasStartNode) {
        // 在画布左上角创建开始节点
        this.addNode(NodeType.START, { x: 100, y: 100 }, '开始');
        this.isDirty = true;
      }
    },

    // 节点操作
    selectNode(nodeId: string | null) {
      this.editorState.selectedNodeId = nodeId;
      this.editorState.selectedCondition = null;
      this.editorState.isEditorPanelOpen = !!nodeId;
    },

    addNode(type: NodeType, position: Position, name: string = '') {
      if (!this.currentWorkflow) {
        this.createWorkflow('New Workflow', 'Created automatically');
      }

      const defaultName = {
        [NodeType.START]: '开始',
        [NodeType.CONDITION]: '条件分支',
        [NodeType.FUNCTION]: '函数',
        [NodeType.END]: '结束'
      }[type];

      const node: Node = {
        id: uuidv4(),
        type,
        name: name || defaultName,
        description: '',
        position,
        nextNodes: {},
        toolName: type === NodeType.FUNCTION ? undefined : undefined,
        context: []
      };

      this.currentWorkflow!.nodes.push(node);
      if (type === NodeType.START) {
        this.currentWorkflow!.startNodeId = node.id;
      }
      this.selectNode(node.id);
      return node;
    },

    addConditionNode(position: Position, name: string = '') {
      const node = this.addNode(NodeType.CONDITION, position, name);
      this.updateNode(node.id, { 
        conditions:  [{
          conditions: [{
            leftOperand: {
              name: '',
              type: VariableType.STRING,
              description: '',
              parent: ''
            },
            operator: '==',
            rightOperand: {
              name: '',
              type: VariableType.STRING,
              description: '',
              defaultValue: '',
              parent: ''
            },
            type: 'CONSTANT'
          }],
          type: 'and',
          hint: '待编辑'
        }, {
          conditions: [] as never[],
          type: 'and' as const,
          hint: 'ELSE'
        }]
      });
    },

    // Add function node with a specific tool
    addFunctionNode(tool: Tool, position: Position, toolName: string) {
      const node = this.addNode(NodeType.FUNCTION, position, tool.name);
      this.updateNode(node.id, { toolName });
      
      // Add tool definition to workflow if not exists
      if (!this.currentWorkflow!.tools.find(t => t.name === toolName)) {
        this.currentWorkflow!.tools.push({
          name: toolName,
          description: tool.description,
          inputs: tool.inputs,
          outputs: tool.outputs
        });
      }
      
      console.log('addFunctionNode', this.currentWorkflow?.nodes);
    },

    updateNode(nodeId: string, updates: Partial<Node>) {
      if (!this.currentWorkflow) return;
      
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (!node) return;

      Object.assign(node, updates);
    },

    deleteNode(nodeId: string) {
      if (!this.currentWorkflow) return;
      
      // 删除所有指向该节点的连接
      this.currentWorkflow.nodes.forEach(node => {
        const updatedNextNodes = { ...node.nextNodes };
        Object.entries(node.nextNodes).forEach(([condition, targetId]) => {
          if (targetId === nodeId) {
            delete updatedNextNodes[condition];
          }
        });
        node.nextNodes = updatedNextNodes;
      });

      // 获取节点所属的工具名称
      const toolName = this.currentWorkflow?.nodes.find(node => node.id === nodeId)?.toolName;

      // 删除节点
      this.currentWorkflow.nodes = this.currentWorkflow.nodes.filter(
        node => node.id !== nodeId
      );

      // 对于函数节点，如果该函数已经没有其它节点在用，则在 workflow.tools 中删除该函数
      if (toolName) {
        if (!this.currentWorkflow?.nodes.some(node => node.toolName === toolName)) {
          const index = this.currentWorkflow.tools.findIndex(tool => tool.name === toolName);
          if (index !== -1) {
            this.currentWorkflow.tools.splice(index, 1);
          }
        }
      }

      if (this.editorState.selectedNodeId === nodeId) {
        this.editorState.selectedNodeId = null;
        this.editorState.selectedCondition = null;
      }
    },

    /** 选中连接线 */
    selectConnection(sourceNodeId: string, condition: string) {
      this.editorState.selectedNodeId = sourceNodeId;
      this.editorState.selectedCondition = condition;
    },

    /** 传递更新节点及其下游节点的context */
    updateNodeContextChain(nodeId: string, visitedNodes: Set<string> = new Set(), upstreamContext?: VariableDefinition[]) {
      if (!this.currentWorkflow || visitedNodes.has(nodeId)) return;
      
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (!node) return;

      visitedNodes.add(nodeId);
      node.context?.push(...upstreamContext||[]);  // 保留已有context，添加上游context
      const newContext: VariableDefinition[] = []
      if (node.type === NodeType.FUNCTION && node.toolName) {
        const toolIndex = this.currentWorkflow.tools?.findIndex(tool => tool.name === node.toolName);
        if (toolIndex !== -1) {
          const tool = this.currentWorkflow.tools[toolIndex];
          Object.entries(tool.outputs).forEach(([_key, value]) => {
            newContext.push({...value, parent: node.id});
          });
        }
      }
      if (Array.isArray(node.context)) {
        newContext.push(...node.context);
      }
      console.log("forward", node.name);
      Object.values(node.nextNodes).forEach(nextNodeId => {
        this.updateNodeContextChain(nextNodeId, visitedNodes, newContext);
      });
    },

    /** 清除节点的context */
    clearNodeContext(nodeId: string) {
      if (!this.currentWorkflow) return;
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (node) {
        node.context = [];
      }
    },

    /** 添加连接 */
    addConnection(sourceNodeId: string, targetNodeId: string, condition: string = 'default') {
      if (!this.currentWorkflow) return;
      
      const sourceNode = this.currentWorkflow.nodes.find(n => n.id === sourceNodeId);
      if (!sourceNode) return;

      // 检查是否已存在相同条件的连接
      if (sourceNode.nextNodes[condition]) return;

      // 对于条件节点，验证连接条件的有效性
      if (sourceNode.type === NodeType.CONDITION && sourceNode.conditions) {
        // Check if it's an empty last condition treated as ELSE
        const lastIndex = sourceNode.conditions.length - 1;
        const isEmptyLastCondition = sourceNode.conditions.length > 0 && 
          condition.startsWith('case') && 
          parseInt(condition.slice(4)) === sourceNode.conditions.length && 
          lastIndex >= 0 &&
          (!sourceNode.conditions[lastIndex].conditions ||
           sourceNode.conditions[lastIndex].conditions.length === 0);
        
        // If it's an empty last condition, treat it as ELSE
        if (isEmptyLastCondition) {
          condition = 'else';
        }
        
        // 检查是否是有效的条件连接
        const isValidCondition = condition === 'else' || 
          (condition.startsWith('case') && 
           parseInt(condition.slice(4)) <= sourceNode.conditions.length);
        
        if (!isValidCondition) return;
      }

      // 旧的后继节点先传播更新
      const oldTargetNode = this.currentWorkflow.nodes.find(n => n.id === targetNodeId);
      if (oldTargetNode) {
        this.updateNodeContextChain(oldTargetNode.id);
      }

      // 添加连接
      sourceNode.nextNodes = {
        ...sourceNode.nextNodes,
        [condition]: targetNodeId
      };

      // 更新目标节点及其下游节点的context
      this.updateNodeContextChain(sourceNodeId);
    },

    /** 删除连接 */
    deleteConnection(sourceNodeId: string, condition: string) {
      console.log('deleteConnection', sourceNodeId, condition);
      if (!this.currentWorkflow) return;
      
      const sourceNode = this.currentWorkflow.nodes.find(n => n.id === sourceNodeId);
      if (!sourceNode) return;

      // Store the target node ID before removing the connection
      const targetNodeId = sourceNode.nextNodes[condition];

      // Remove the connection
      const { [condition]: removed, ...remainingConnections } = sourceNode.nextNodes;
      sourceNode.nextNodes = remainingConnections;

      // If there was a target node, update its and downstream nodes' context
      console.log("deleteConnection", targetNodeId);
      if (targetNodeId) {
        this.clearNodeContext(targetNodeId);
        this.updateNodeContextChain(targetNodeId);
      }

      // Mark workflow as modified
      this.isDirty = true;
    },

    // 编辑器状态操作
    updateCanvasState(updates: Partial<EditorState['canvasState']>, mousePosition?: { x: number, y: number }) {
      if (updates.scale && mousePosition && this.editorState.canvasState.scale) {
        // 计算缩放前的鼠标世界坐标
        const oldScale = this.editorState.canvasState.scale;
        const oldPos = this.editorState.canvasState.position;
        const worldX = (mousePosition.x - oldPos.x) / oldScale;
        const worldY = (mousePosition.y - oldPos.y) / oldScale;
        
        // 计算新的画布位置，以保持鼠标位置下的点不变
        const newScale = updates.scale;
        const newX = mousePosition.x - worldX * newScale;
        const newY = mousePosition.y - worldY * newScale;
        
        this.editorState.canvasState = {
          ...this.editorState.canvasState,
          ...updates,
          position: { x: newX, y: newY }
        };
      } else {
        Object.assign(this.editorState.canvasState, updates);
      }
    },

    /** 保存工作流 */
    async saveWorkflow() {
      if (!this.currentWorkflow) return null;
      
      try {
        // Create a copy of the workflow for saving
        const workflowToSave = { ...this.currentWorkflow };
        
        // Save the workflow
        const savedWorkflow = await workflowApi.saveWorkflow(convertAppToApiWorkflow(workflowToSave));
        
        // Reload the workflow to ensure consistency
        // this.updateWorkflow(convertApiToAppWorkflow(savedWorkflow));
        this.isDirty = false;
        return savedWorkflow;
      } catch (error) {
        console.error('Failed to save workflow:', error);
        throw error;
      }
    },

    /** 更新节点位置 */
    updateNodePosition(nodeId: string, position: Position) {
      if (!this.currentWorkflow) return;
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (node) {
        node.position = position;
      }
    },

    // Initialize and propagate context from start node
    initializeWorkflowContext() {
      if (!this.currentWorkflow?.startNodeId) return;
      
      // Clear all nodes' context first
      this.currentWorkflow?.nodes.forEach(node => {
        node.context = [];
      });

      // 获取开始节点
      const startNode = this.currentWorkflow?.nodes.find(n => n.id === this.currentWorkflow?.startNodeId);
      if (!startNode) return;

      this.updateNodeContextChain(startNode.id);
    }
  }
}); 
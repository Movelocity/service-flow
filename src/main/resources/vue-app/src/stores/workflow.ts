import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { Node, Workflow, Position, EditorState } from '../types/workflow';
import type { Tool } from '../services/toolApi';
import { NodeType } from '../types/workflow';
import { WorkflowApi } from '../services/workflowApi';

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
        nodes: []
      };
    },

    async loadWorkflow(id: string) {
      const workflow = await workflowApi.getWorkflow(id);
      this.updateWorkflow(workflow);
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
      
      const node: Node = {
        id: uuidv4(),
        type,
        name: name || `${type} Node`,
        description: '',
        position,
        parameters: {},
        nextNodes: {}
      };

      this.currentWorkflow!.nodes.push(node);
      this.selectNode(node.id);
      return node;
    },

    // Add function node with a specific tool
    addFunctionNode(tool: Tool, position: Position) {
      const node = this.addNode(NodeType.FUNCTION, position, tool.name);
      this.updateNodeParameters(node.id, { tool });
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

      // 删除节点
      this.currentWorkflow.nodes = this.currentWorkflow.nodes.filter(
        node => node.id !== nodeId
      );

      if (this.editorState.selectedNodeId === nodeId) {
        this.editorState.selectedNodeId = null;
        this.editorState.selectedCondition = null;
      }
    },

    // 连接操作
    selectConnection(sourceNodeId: string, condition: string) {
      this.editorState.selectedNodeId = sourceNodeId;
      this.editorState.selectedCondition = condition;
    },

    addConnection(sourceNodeId: string, targetNodeId: string, condition: string = 'default') {
      if (!this.currentWorkflow) return;
      
      const sourceNode = this.currentWorkflow.nodes.find(n => n.id === sourceNodeId);
      if (!sourceNode) return;

      // 检查是否已存在相同条件的连接
      if (sourceNode.nextNodes[condition]) return;

      // 添加连接
      sourceNode.nextNodes = {
        ...sourceNode.nextNodes,
        [condition]: targetNodeId
      };
    },

    deleteConnection(sourceNodeId: string, condition: string) {
      if (!this.currentWorkflow) return;

      const sourceNode = this.currentWorkflow.nodes.find(n => n.id === sourceNodeId);
      if (!sourceNode) return;

      const { [condition]: _, ...remainingNextNodes } = sourceNode.nextNodes;
      sourceNode.nextNodes = remainingNextNodes;

      if (this.editorState.selectedNodeId === sourceNodeId && 
          this.editorState.selectedCondition === condition) {
        this.editorState.selectedCondition = null;
      }
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

    async saveWorkflow() {
      if (!this.currentWorkflow) return null;
      
      try {
        // Create a copy of the workflow for saving
        const workflowToSave = { ...this.currentWorkflow };
        
        // Save the workflow
        const savedWorkflow = await workflowApi.saveWorkflow(workflowToSave);
        
        // Reload the workflow to ensure consistency
        this.updateWorkflow(savedWorkflow);
        this.isDirty = false;
        return savedWorkflow;
      } catch (error) {
        console.error('Failed to save workflow:', error);
        throw error;
      }
    },

    updateNodePosition(nodeId: string, position: Position) {
      if (!this.currentWorkflow) return;
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (node) {
        node.position = position;
      }
    },

    updateNodeParameters(nodeId: string, parameters: Record<string, any>) {
      if (!this.currentWorkflow) return;
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (node) {
        node.parameters = parameters;
      }
    }
  }
}); 
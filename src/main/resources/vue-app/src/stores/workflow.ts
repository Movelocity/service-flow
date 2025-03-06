import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { Node, Connection, Workflow, Position, EditorState } from '../types/workflow';
import { NodeType } from '../types/workflow';

interface WorkflowState {
  currentWorkflow: Workflow | null;
  editorState: EditorState;
  history: {
    past: Workflow[];
    future: Workflow[];
  };
}

export const useWorkflowStore = defineStore('workflow', {
  state: (): WorkflowState => ({
    currentWorkflow: null,
    editorState: {
      selectedNodeId: null,
      isEditorPanelOpen: false,
      canvasState: {
        scale: 1,
        position: { x: 0, y: 0 },
        isDragging: false
      }
    },
    history: {
      past: [],
      future: []
    }
  }),

  getters: {
    selectedNode: (state): Node | null => {
      if (!state.currentWorkflow || !state.editorState.selectedNodeId) return null;
      return state.currentWorkflow.nodes.find(node => node.id === state.editorState.selectedNodeId) || null;
    },

    nodeConnections: (state) => (nodeId: string): Connection[] => {
      if (!state.currentWorkflow) return [];
      return state.currentWorkflow.connections.filter(
        conn => conn.sourceNodeId === nodeId || conn.targetNodeId === nodeId
      );
    }
  },

  actions: {
    // 工作流基本操作
    createWorkflow(name: string, description: string) {
      this.currentWorkflow = {
        id: uuidv4(),
        name,
        description,
        nodes: [],
        connections: []
      };
      this.saveToHistory();
    },

    loadWorkflow(workflow: Workflow) {
      this.currentWorkflow = workflow;
      this.history.past = [];
      this.history.future = [];
      this.saveToHistory();
    },

    // 节点操作
    addNode(type: NodeType, position: Position, name: string = '') {
      if (!this.currentWorkflow) return;
      
      const node: Node = {
        id: uuidv4(),
        type,
        name: name || `${type} Node`,
        position,
        parameters: {}
      };

      this.currentWorkflow.nodes.push(node);
      this.saveToHistory();
      return node;
    },

    updateNode(nodeId: string, updates: Partial<Node>) {
      if (!this.currentWorkflow) return;
      
      const node = this.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (!node) return;

      Object.assign(node, updates);
      this.saveToHistory();
    },

    deleteNode(nodeId: string) {
      if (!this.currentWorkflow) return;
      
      this.currentWorkflow.nodes = this.currentWorkflow.nodes.filter(n => n.id !== nodeId);
      this.currentWorkflow.connections = this.currentWorkflow.connections.filter(
        c => c.sourceNodeId !== nodeId && c.targetNodeId !== nodeId
      );
      
      if (this.editorState.selectedNodeId === nodeId) {
        this.editorState.selectedNodeId = null;
        this.editorState.isEditorPanelOpen = false;
      }
      
      this.saveToHistory();
    },

    // 连接操作
    addConnection(sourceId: string, targetId: string, condition?: string) {
      if (!this.currentWorkflow) return;
      
      // 防止自我连接和重复连接
      if (sourceId === targetId) return;
      if (this.currentWorkflow.connections.some(
        c => c.sourceNodeId === sourceId && c.targetNodeId === targetId
      )) return;

      const connection: Connection = {
        id: uuidv4(),
        sourceNodeId: sourceId,
        targetNodeId: targetId,
        condition
      };

      this.currentWorkflow.connections.push(connection);
      this.saveToHistory();
      return connection;
    },

    deleteConnection(connectionId: string) {
      if (!this.currentWorkflow) return;
      
      this.currentWorkflow.connections = this.currentWorkflow.connections.filter(
        c => c.id !== connectionId
      );
      this.saveToHistory();
    },

    // 历史记录操作
    saveToHistory() {
      if (!this.currentWorkflow) return;
      
      this.history.past.push(JSON.parse(JSON.stringify(this.currentWorkflow)));
      this.history.future = [];
      
      // 限制历史记录数量
      if (this.history.past.length > 20) {
        this.history.past.shift();
      }
    },

    undo() {
      if (this.history.past.length === 0) return;
      
      const current = this.currentWorkflow;
      if (current) {
        this.history.future.push(JSON.parse(JSON.stringify(current)));
      }
      
      this.currentWorkflow = this.history.past.pop() || null;
    },

    redo() {
      if (this.history.future.length === 0) return;
      
      const current = this.currentWorkflow;
      if (current) {
        this.history.past.push(JSON.parse(JSON.stringify(current)));
      }
      
      this.currentWorkflow = this.history.future.pop() || null;
    },

    // 编辑器状态操作
    setSelectedNode(nodeId: string | null) {
      this.editorState.selectedNodeId = nodeId;
      this.editorState.isEditorPanelOpen = !!nodeId;
    },

    updateCanvasState(updates: Partial<EditorState['canvasState']>) {
      Object.assign(this.editorState.canvasState, updates);
    }
  }
}); 
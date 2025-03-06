import { defineStore } from 'pinia';
import { v4 as uuidv4 } from 'uuid';
import type { Node, Connection, Workflow, Position, EditorState } from '../types/workflow';
import { NodeType } from '../types/workflow';

interface WorkflowState {
  currentWorkflow: Workflow | null;
  editorState: EditorState & {
    selectedConnectionId: string | null;
  };
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
      selectedConnectionId: null,
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

    selectedConnection: (state): Connection | null => {
      if (!state.currentWorkflow || !state.editorState.selectedConnectionId) return null;
      return state.currentWorkflow.connections.find(conn => conn.id === state.editorState.selectedConnectionId) || null;
    },

    nodeConnections: (state) => (nodeId: string): Connection[] => {
      if (!state.currentWorkflow?.connections) return [];
      return state.currentWorkflow.connections.filter(
        conn => conn.sourceNodeId === nodeId || conn.targetNodeId === nodeId
      );
    },

    // 获取节点的输入连接
    nodeInputConnections: (state) => (nodeId: string): Connection[] => {
      if (!state.currentWorkflow?.connections) return [];
      return state.currentWorkflow.connections.filter(
        conn => conn.targetNodeId === nodeId
      );
    },

    // 获取节点的输出连接
    nodeOutputConnections: (state) => (nodeId: string): Connection[] => {
      if (!state.currentWorkflow?.connections) return [];
      return state.currentWorkflow.connections.filter(
        conn => conn.sourceNodeId === nodeId
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
      // Convert nextNodes to connections if they exist
      const connections = [...(workflow.connections || [])];
      workflow.nodes.forEach(node => {
        if (node.nextNodes) {
          Object.entries(node.nextNodes).forEach(([condition, targetNodeId]) => {
            if (!connections.some(conn => 
              conn.sourceNodeId === node.id && 
              conn.targetNodeId === targetNodeId
            )) {
              connections.push({
                id: `conn_${Date.now()}_${node.id}_${targetNodeId}`,
                sourceNodeId: node.id,
                targetNodeId: targetNodeId,
                condition: condition === 'default' ? undefined : condition
              });
            }
          });
          // Remove the nextNodes as they are now converted to connections
          delete node.nextNodes;
        }
      });

      this.currentWorkflow = {
        ...workflow,
        connections
      };
      this.history.past = [];
      this.history.future = [];
      this.saveToHistory();
    },

    // 节点操作
    selectNode(nodeId: string | null) {
      this.editorState.selectedNodeId = nodeId;
      if (nodeId) {
        this.editorState.selectedConnectionId = null;
      }
    },

    addNode(type: NodeType, position: Position, name: string = '') {
      if (!this.currentWorkflow) {
        this.createWorkflow('New Workflow', 'Created automatically');
      }
      
      const node: Node = {
        id: uuidv4(),
        type,
        name: name || `${type} Node`,
        position,
        parameters: {}
      };

      this.currentWorkflow!.nodes.push(node);
      this.selectNode(node.id);
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
    selectConnection(connectionId: string | null) {
      this.editorState.selectedConnectionId = connectionId;
      if (connectionId) {
        this.editorState.selectedNodeId = null;
      }
    },

    addConnection(sourceNodeId: string, targetNodeId: string, condition?: string) {
      if (!this.currentWorkflow) return;
      
      // 防止自我连接和重复连接
      if (sourceNodeId === targetNodeId) return;
      if (this.currentWorkflow.connections.some(
        c => c.sourceNodeId === sourceNodeId && c.targetNodeId === targetNodeId
      )) return;

      const newConnection: Connection = {
        id: `conn_${Date.now()}`,
        sourceNodeId,
        targetNodeId,
        condition
      };

      this.currentWorkflow.connections.push(newConnection);
      this.selectConnection(newConnection.id);
      this.saveToHistory();
      return newConnection;
    },

    deleteConnection(connectionId: string) {
      if (!this.currentWorkflow) return;
      
      const index = this.currentWorkflow.connections.findIndex(conn => conn.id === connectionId);
      if (index !== -1) {
        this.currentWorkflow.connections.splice(index, 1);
      }

      if (this.editorState.selectedConnectionId === connectionId) {
        this.editorState.selectedConnectionId = null;
      }
      
      this.saveToHistory();
    },

    updateConnection(connectionId: string, updates: Partial<Connection>) {
      if (!this.currentWorkflow) return;

      const connection = this.currentWorkflow.connections.find(conn => conn.id === connectionId);
      if (connection) {
        Object.assign(connection, updates);
      }
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
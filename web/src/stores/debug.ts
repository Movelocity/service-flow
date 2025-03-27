import { defineStore } from 'pinia';
import { WorkflowApi } from '@/services/workflowApi';
import type { NodeExecutionEvent } from '@/types/debug';

const workflowApi = new WorkflowApi();

// Debug state interface
interface DebugState {
  isDebugging: boolean;
  debugEvents: NodeExecutionEvent[];
  workflowInputs: Record<string, any>;
  eventSource: (() => void) | null;
  currentWorkflowId: string | null;
  _handleNodeExecution: ((event: Event) => void) | null;
  _handleDebugError: ((event: Event) => void) | null;
  runningNodeId: string | null; // Track currently executing node
  showInputForm: boolean;
}

export const useDebugStore = defineStore('debug', {
  state: (): DebugState => ({
    isDebugging: false,
    debugEvents: [],
    workflowInputs: {},
    eventSource: null,
    currentWorkflowId: null,
    _handleNodeExecution: null,
    _handleDebugError: null,
    runningNodeId: null,
    showInputForm: true
  }),

  getters: {
    canTest: (state) => (startNodeExists: boolean, endNodeExists: boolean): boolean => {
      if (!state.currentWorkflowId) return false;
      return startNodeExists && endNodeExists;
    }
  },

  actions: {
    /**
     * 初始化调试，获取工作流必填参数
     */
    async initDebug(workflowId: string) {
      if (!workflowId) return;
      
      this.currentWorkflowId = workflowId;
      
      if (!this.isDebugging) {
        try {
          // 获取工作流必填参数
          this.workflowInputs = await workflowApi.getWorkflowInputs(workflowId);
          this.isDebugging = true;
          this.debugEvents = [];
          this.runningNodeId = null; // Reset running node
          this.showInputForm = true; // 显示输入表单
        } catch (error) {
          console.error('Failed to get workflow inputs:', error);
          throw new Error('获取工作流参数失败');
        }
      } else {
        this.stopDebug();
      }
    },

    /**
     * 开始调试工作流
     */
    async startDebug(inputs: Record<string, any>) {
      if (!this.currentWorkflowId) return;
      
      try {
        // 标记输入表单已处理
        this.showInputForm = false;
        
        // 注册事件监听器
        const handleNodeExecution = ((event: Event) => {
          if (event instanceof CustomEvent) {
            const nodeEvent = event.detail as NodeExecutionEvent;
            console.log('handleNodeExecution', nodeEvent);
            
            // 更新正在执行的节点
            if (nodeEvent.eventType === 'ENTER') {
              // 节点开始执行时，设置为当前正在执行的节点
              this.runningNodeId = nodeEvent.nodeId;
              console.log('runningNodeId', this.runningNodeId);
            } else if (nodeEvent.eventType === 'COMPLETE') {
              // 如果是当前正在执行的节点完成了，则清除正在执行的节点
              if (this.runningNodeId === nodeEvent.nodeId) {
                this.runningNodeId = null;
              }
            }
            
            this.debugEvents.push(nodeEvent);
          }
        }) as EventListener;

        const handleDebugError = ((event: Event) => {
          if (event instanceof CustomEvent) {
            console.error('Debug error:', event.detail);
            this.runningNodeId = null; // Clear running node on error
            this.stopDebug();
            throw new Error('调试过程中发生错误');
          }
        }) as EventListener;

        window.addEventListener('node-execution', handleNodeExecution);
        window.addEventListener('debug-error', handleDebugError);

        // 保存清理函数
        this.eventSource = workflowApi.debugWorkflow(this.currentWorkflowId, inputs);
        
        // 添加清理函数到store
        this._handleNodeExecution = handleNodeExecution;
        this._handleDebugError = handleDebugError;
      } catch (error) {
        console.error('Failed to start workflow debug:', error);
        this.stopDebug();
        throw new Error('启动调试失败');
      }
    },

    /**
     * 停止调试
     */
    stopDebug() {
      if (this.eventSource) {
        // 移除事件监听器
        if (this._handleNodeExecution) {
          window.removeEventListener('node-execution', this._handleNodeExecution as EventListener);
          this._handleNodeExecution = null;
        }
        if (this._handleDebugError) {
          window.removeEventListener('debug-error', this._handleDebugError as EventListener);
          this._handleDebugError = null;
        }
        // 调用清理函数关闭连接
        this.eventSource();
        this.eventSource = null;
      }
      this.isDebugging = false;
      this.runningNodeId = null; // Clear running node when stopping debug
      this.showInputForm = true; // Reset input form state
    },

    /**
     * 重置调试状态但保持调试面板打开
     */
    resetDebugEvents() {
      // 如果有正在运行的调试，停止它但不改变isDebugging状态
      if (this.eventSource) {
        // 移除事件监听器
        if (this._handleNodeExecution) {
          window.removeEventListener('node-execution', this._handleNodeExecution as EventListener);
          this._handleNodeExecution = null;
        }
        if (this._handleDebugError) {
          window.removeEventListener('debug-error', this._handleDebugError as EventListener);
          this._handleDebugError = null;
        }
        // 调用清理函数关闭连接
        this.eventSource();
        this.eventSource = null;
      }
      
      // 清空调试事件但保持调试状态
      this.debugEvents = [];
      this.runningNodeId = null;
      // 重新显示输入表单
      this.showInputForm = true;
    },

    /**
     * 重置调试状态
     */
    resetDebug() {
      this.stopDebug();
      this.debugEvents = [];
      this.workflowInputs = {};
      this.currentWorkflowId = null;
      this.runningNodeId = null; // Clear running node when resetting
      this.showInputForm = true; // Reset input form state
    }
  }
}); 
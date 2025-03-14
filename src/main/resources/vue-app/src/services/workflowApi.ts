/// <reference types="vite/client" />
import type { ApiWorkflow } from '../types/workflow';
import { fetchEventSource } from '@microsoft/fetch-event-source';

/**
 * Workflow API Service
 * Handles all workflow-related API calls
 */
export class WorkflowApi {
  /**
   * Base URL for workflow API endpoints
   * Uses localhost:8080 in development and relative path in production
   */
  private baseUrl: string;

  constructor(baseUrl: string = '/api/workflows') {
    this.baseUrl = baseUrl;
  }

  /**
   * Fetch a workflow by ID
   */
  async getWorkflow(workflowId: string): Promise<ApiWorkflow> {
    const response = await fetch(`${this.baseUrl}/${workflowId}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch workflow: ${response.statusText}`);
    }
    return await response.json();
  }

  /**
   * List all workflows
   */
  async listWorkflows(): Promise<ApiWorkflow[]> {
    const response = await fetch(this.baseUrl);
    if (!response.ok) {
      throw new Error(`Failed to list workflows: ${response.statusText}`);
    }
    const workflow_ids = await response.json();
    const workflows = [];
    for (const workflow_id of workflow_ids) {
      const workflow = await this.getWorkflow(workflow_id);
      workflows.push(workflow);
    }
    
    return workflows;
  }

  /**
   * Save a new workflow or update an existing one
   */
  async saveWorkflow(workflow: Partial<ApiWorkflow>): Promise<ApiWorkflow> {
    // 剔除节点中的context
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(workflow)
    });
    if (!response.ok) {
      throw new Error(`Failed to save workflow: ${response.statusText}`);
    }
    return await response.json();
  }

  /**
   * Delete a workflow
   */
  async deleteWorkflow(workflowId: string): Promise<boolean> {
    const response = await fetch(`${this.baseUrl}/${workflowId}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      throw new Error(`Failed to delete workflow: ${response.statusText}`);
    }

    const result = await response.json();
    return result.success;
  }

  /**
   * Start workflow execution
   */
  async executeWorkflow(workflowId: string, input?: Record<string, any>): Promise<string> {
    const response = await fetch(`${this.baseUrl}/${workflowId}/execute`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(input || {})
    });

    if (!response.ok) {
      throw new Error(`Failed to execute workflow: ${response.statusText}`);
    }

    const result = await response.json();
    return result.executionId;
  }

  /**
   * Get workflow execution status
   */
  async getExecutionStatus(workflowId: string, executionId: string): Promise<string> {
    const response = await fetch(`${this.baseUrl}/${workflowId}/executions/${executionId}`);

    if (!response.ok) {
      throw new Error(`Failed to get execution status: ${response.statusText}`);
    }

    const result = await response.json();
    return result.status;
  }

  /**
   * Start workflow execution in debug mode
   * @returns A function to close the event source connection
   */
  debugWorkflow(workflowId: string, input?: Record<string, any>): () => void {
    let ctrl = new AbortController();
    
    fetchEventSource(`${this.baseUrl}/${workflowId}/debug`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
      },
      body: JSON.stringify(input || {}),
      signal: ctrl.signal,
      onmessage(event) {
        // 触发自定义事件
        const customEvent = new CustomEvent('node-execution', {
          detail: JSON.parse(event.data)
        });
        window.dispatchEvent(customEvent);
      },
      onclose() {
        // 当连接关闭时
        console.log('Debug connection closed');
      },
      onerror(err) {
        // 当发生错误时
        console.error('Debug connection error:', err);
        // 触发错误事件
        const errorEvent = new CustomEvent('debug-error', {
          detail: err
        });
        window.dispatchEvent(errorEvent);
        // 禁止重试
        ctrl.abort();
      },
      openWhenHidden: true // 在页面隐藏时保持连接，避免切换回来重新发起
    }).catch(err => {
      console.error('Failed to start debug:', err);
      throw err;
    });

    // 返回一个函数用于关闭连接
    return () => {
      ctrl.abort();
    };
  }

  /**
   * Get workflow required inputs
   * @returns Required input parameters
   */
  async getWorkflowInputs(workflowId: string): Promise<Record<string, any>> {
    const response = await fetch(`${this.baseUrl}/${workflowId}`);
    if (!response.ok) {
      throw new Error('Failed to get workflow inputs');
    }
    const workflow = await response.json();
    return workflow.inputs || {};
  }
}

// Export a singleton instance
export const workflowApi = new WorkflowApi(); 
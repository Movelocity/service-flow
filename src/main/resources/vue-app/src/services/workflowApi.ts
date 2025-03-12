/// <reference types="vite/client" />
import type { ApiWorkflow } from '../types/workflow';

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
   * @returns EventSource for receiving debug events
   */
  debugWorkflow(workflowId: string, input?: Record<string, any>): EventSource {
    const params = new URLSearchParams();
    if (input) {
      Object.entries(input).forEach(([key, value]) => {
        params.append(key, JSON.stringify(value));
      });
    }

    const eventSource = new EventSource(
      `${this.baseUrl}/${workflowId}/debug?${params.toString()}`
    );

    eventSource.addEventListener('node-execution', (event) => {
      const data = JSON.parse(event.data);
      // Event data will be handled by the caller
    });

    return eventSource;
  }
}

// Export a singleton instance
export const workflowApi = new WorkflowApi(); 
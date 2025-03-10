/// <reference types="vite/client" />
import type { Workflow } from '../types/workflow';

/**
 * Workflow API Service
 * Handles all workflow-related API calls
 */
export class WorkflowApi {
  /**
   * Base URL for workflow API endpoints
   * Uses localhost:8080 in development and relative path in production
   */
  private baseUrl = import.meta.env.DEV 
    ? 'http://localhost:8080/api/workflows'
    : '/api/workflows';

  /**
   * Fetch a workflow by ID
   */
  async getWorkflow(workflowId: string): Promise<Workflow> {
    const response = await fetch(`${this.baseUrl}/${workflowId}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch workflow: ${response.statusText}`);
    }
    return await response.json();
  }

  /**
   * List all workflows
   */
  async listWorkflows(): Promise<Workflow[]> {
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
  async saveWorkflow(workflow: Partial<Workflow>): Promise<Workflow> {
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
}

// Export a singleton instance
export const workflowApi = new WorkflowApi(); 
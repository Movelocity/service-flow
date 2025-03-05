/**
 * Workflow API Service
 * Handles API interactions with the server for workflow operations
 */

const WorkflowAPI = {
  // Base API URL
  baseUrl: '/api/workflows',
  
  // Fetch a workflow from the server
  async fetchWorkflow(workflowId) {
    try {
      const response = await fetch(`${this.baseUrl}/${workflowId}`);
      if (!response.ok) {
        throw new Error(`Failed to fetch workflow: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Error fetching workflow:', error);
      throw error;
    }
  },
  
  // Save a workflow to the server
  async saveWorkflow(workflow) {
    try {
      const method = workflow.id ? 'PUT' : 'POST';
      const url = workflow.id ? `${this.baseUrl}/${workflow.id}` : this.baseUrl;
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(workflow)
      });
      
      if (!response.ok) {
        throw new Error(`Failed to save workflow: ${response.statusText}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error saving workflow:', error);
      throw error;
    }
  },
  
  // Delete a workflow from the server
  async deleteWorkflow(workflowId) {
    try {
      const response = await fetch(`${this.baseUrl}/${workflowId}`, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        throw new Error(`Failed to delete workflow: ${response.statusText}`);
      }
      
      return true;
    } catch (error) {
      console.error('Error deleting workflow:', error);
      throw error;
    }
  },
  
  // Start a workflow execution
  async startWorkflowExecution(workflowId, input) {
    try {
      const response = await fetch(`${this.baseUrl}/${workflowId}/execute`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(input)
      });
      
      if (!response.ok) {
        throw new Error(`Failed to start workflow execution: ${response.statusText}`);
      }
      
      const result = await response.json();
      return result.executionId;
    } catch (error) {
      console.error('Error starting workflow execution:', error);
      throw error;
    }
  },
  
  // Get workflow execution status
  async getWorkflowExecutionStatus(workflowId, executionId) {
    try {
      const response = await fetch(`${this.baseUrl}/${workflowId}/executions/${executionId}`);
      
      if (!response.ok) {
        throw new Error(`Failed to get execution status: ${response.statusText}`);
      }
      
      const result = await response.json();
      return result.status;
    } catch (error) {
      console.error('Error getting workflow execution status:', error);
      throw error;
    }
  }
};

// Export the WorkflowAPI object
window.WorkflowAPI = WorkflowAPI; 
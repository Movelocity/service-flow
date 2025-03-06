import axios from 'axios'

/**
 * Workflow API Service
 * Handles API interactions with the server for workflow operations
 */
export default {
  // Base API URL - use localhost in development, relative path in production
  get baseUrl() {
    const baseUrl = import.meta.env.DEV 
      ? 'http://localhost:8080/api/workflows'
      : '/api/workflows'
    console.log('baseUrl', baseUrl)
    return baseUrl
  },
  
  // Axios request config with credentials
  axiosConfig: {
    withCredentials: true
  },
  
  /**
   * Fetch all workflows from the server
   * @returns {Promise<string[]>} - Array of workflow IDs
   */
  async fetchWorkflows() {
    try {
      const response = await axios.get(this.baseUrl, this.axiosConfig)
      return response.data
    } catch (error) {
      console.error('Error fetching workflows:', error)
      throw error
    }
  },
  
  /**
   * Fetch a workflow from the server
   * @param {string} workflowId - The ID of the workflow to fetch
   * @returns {Promise<Object>} - The workflow data
   */
  async fetchWorkflow(workflowId: string) {
    try {
      const response = await axios.get(`${this.baseUrl}/${workflowId}`, this.axiosConfig)
      return response.data
    } catch (error) {
      console.error('Error fetching workflow:', error)
      throw error
    }
  },
  
  /**
   * Save a workflow to the server
   * @param {Object} workflow - The workflow data to save
   * @returns {Promise<Object>} - The saved workflow data
   */
  async saveWorkflow(workflow: any) {
    try {
      const method = workflow.id ? 'put' : 'post'
      const url = workflow.id ? `${this.baseUrl}/${workflow.id}` : this.baseUrl
      
      const response = await axios[method](url, workflow, this.axiosConfig)
      return response.data
    } catch (error) {
      console.error('Error saving workflow:', error)
      throw error
    }
  },
  
  /**
   * Delete a workflow from the server
   * @param {string} workflowId - The ID of the workflow to delete
   * @returns {Promise<boolean>} - True if successful
   */
  async deleteWorkflow(workflowId: string) {
    try {
      await axios.delete(`${this.baseUrl}/${workflowId}`, this.axiosConfig)
      return true
    } catch (error) {
      console.error('Error deleting workflow:', error)
      throw error
    }
  },
  
  /**
   * Start a workflow execution
   * @param {string} workflowId - The ID of the workflow to execute
   * @param {Object} input - The input data for the workflow
   * @returns {Promise<string>} - The execution ID
   */
  async startWorkflowExecution(workflowId: string, input: any) {
    try {
      const response = await axios.post(`${this.baseUrl}/${workflowId}/execute`, input, this.axiosConfig)
      return response.data.executionId
    } catch (error) {
      console.error('Error starting workflow execution:', error)
      throw error
    }
  },
  
  /**
   * Get workflow execution status
   * @param {string} workflowId - The ID of the workflow
   * @param {string} executionId - The ID of the execution
   * @returns {Promise<string>} - The execution status
   */
  async getWorkflowExecutionStatus(workflowId: string, executionId: string) {
    try {
      const response = await axios.get(`${this.baseUrl}/${workflowId}/executions/${executionId}`, this.axiosConfig)
      return response.data.status
    } catch (error) {
      console.error('Error getting workflow execution status:', error)
      throw error
    }
  }
} 
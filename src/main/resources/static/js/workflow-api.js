/**
 * Workflow API Service
 * Handles all API interactions for the workflow editor
 */

// API endpoints
const API_ENDPOINTS = {
  WORKFLOWS: '/api/workflows',
  WORKFLOW: (id) => `/api/workflows/${id}`,
  START_WORKFLOW: (id) => `/api/workflows/${id}/start`,
  WORKFLOW_STATUS: (id, executionId) => `/api/workflows/${id}/status/${executionId}`
};

/**
 * Loads a workflow from the server
 * @param {string} workflowId - The ID of the workflow to load
 * @returns {Promise<Object>} - The workflow object
 */
async function fetchWorkflow(workflowId) {
  const response = await fetch(API_ENDPOINTS.WORKFLOW(workflowId));
  if (!response.ok) {
    throw new Error('Workflow not found');
  }
  return await response.json();
}

/**
 * Saves a workflow to the server
 * @param {Object} workflow - The workflow to save
 * @returns {Promise<Object>} - The response from the server
 */
async function saveWorkflowToServer(workflow) {
  const response = await fetch(API_ENDPOINTS.WORKFLOWS, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(workflow)
  });

  if (!response.ok) {
    throw new Error('Failed to save workflow');
  }
  
  return await response.json();
}

/**
 * Deletes a workflow from the server
 * @param {string} workflowId - The ID of the workflow to delete
 * @returns {Promise<void>}
 */
async function deleteWorkflowFromServer(workflowId) {
  const response = await fetch(API_ENDPOINTS.WORKFLOW(workflowId), {
    method: 'DELETE'
  });

  if (!response.ok) {
    throw new Error('Failed to delete workflow');
  }
}

/**
 * Starts a workflow execution
 * @param {string} workflowId - The ID of the workflow to start
 * @param {Object} input - The input data for the workflow
 * @returns {Promise<string>} - The execution ID
 */
async function startWorkflowExecution(workflowId, input = {}) {
  const response = await fetch(API_ENDPOINTS.START_WORKFLOW(workflowId), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(input)
  });

  if (!response.ok) {
    throw new Error('Failed to start workflow execution');
  }
  
  return await response.text();
}

/**
 * Gets the status of a workflow execution
 * @param {string} workflowId - The ID of the workflow
 * @param {string} executionId - The ID of the execution
 * @returns {Promise<string>} - The status of the execution
 */
async function getWorkflowExecutionStatus(workflowId, executionId) {
  const response = await fetch(API_ENDPOINTS.WORKFLOW_STATUS(workflowId, executionId));
  
  if (!response.ok) {
    throw new Error('Failed to get workflow execution status');
  }
  
  return await response.text();
} 
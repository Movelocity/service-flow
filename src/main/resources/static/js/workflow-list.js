/**
 * Workflow List Management
 * Handles displaying and managing workflows on the main page
 */

document.addEventListener('DOMContentLoaded', () => {
  // Load the list of workflows when the page loads
  loadWorkflowList();
  
  // Set up event listeners
  document.getElementById('createWorkflowBtn').addEventListener('click', createNewWorkflow);
});

/**
 * Loads the list of workflows from the server and displays them
 */
async function loadWorkflowList() {
  try {
    const response = await fetch('/api/workflows');
    const workflows = await response.json();
    
    const workflowList = document.getElementById('workflowList');
    const noWorkflowsMessage = document.getElementById('noWorkflowsMessage');
    
    // Clear the current list
    workflowList.innerHTML = '';
    
    if (workflows.length === 0) {
      // Show the "no workflows" message if there are no workflows
      workflowList.appendChild(noWorkflowsMessage);
      return;
    }
    
    // Hide the "no workflows" message
    if (noWorkflowsMessage) {
      noWorkflowsMessage.remove();
    }
    
    // Create a card for each workflow
    workflows.forEach(async (workflowId) => {
      try {
        // Get the details for this workflow
        const detailsResponse = await fetch(`/api/workflows/${workflowId}`);
        const workflow = await detailsResponse.json();
        
        // Create a card for this workflow
        const card = createWorkflowCard(workflow);
        workflowList.appendChild(card);
      } catch (error) {
        console.error(`Error loading workflow ${workflowId}:`, error);
      }
    });
  } catch (error) {
    console.error('Error loading workflow list:', error);
    alert('Failed to load workflow list');
  }
}

/**
 * Creates a card element for a workflow
 * @param {Object} workflow - The workflow data
 * @returns {HTMLElement} - The card element
 */
function createWorkflowCard(workflow) {
  const col = document.createElement('div');
  col.className = 'col-md-4 mb-4';
  
  const card = document.createElement('div');
  card.className = 'card workflow-card h-100';
  card.dataset.workflowId = workflow.id;
  
  // Count the number of nodes by type
  const nodeCounts = {
    START: 0,
    FUNCTION: 0,
    CONDITION: 0,
    END: 0
  };
  
  workflow.nodes.forEach(node => {
    if (nodeCounts.hasOwnProperty(node.type)) {
      nodeCounts[node.type]++;
    }
  });
  
  card.innerHTML = `
    <div class="card-body">
      <h5 class="card-title">${workflow.name || 'Unnamed Workflow'}</h5>
      <p class="card-text">${workflow.description || 'No description'}</p>
      <span class="card-subtitle mb-2 text-muted">ID: ${workflow.id}</span>
      <div class="d-flex justify-content-between">
        <small class="text-muted">Status: ${workflow.isActive ? 'Active' : 'Inactive'}</small>
        <small class="text-muted">Nodes: ${workflow.nodes.length}</small>
      </div>
    </div>
    <div class="card-footer bg-transparent d-flex justify-content-end gap-2">
      <button class="btn btn-sm btn-outline-primary edit-btn">编辑</button>
      <button class="btn btn-sm btn-outline-success test-btn">测试</button>
      <button class="btn btn-sm btn-outline-danger delete-btn">删除</button>
    </div>
  `;
  
  // Add event listeners to the buttons
  card.querySelector('.edit-btn').addEventListener('click', (e) => {
    e.stopPropagation();
    openWorkflowEditor(workflow.id);
  });
  
  card.querySelector('.test-btn').addEventListener('click', (e) => {
    e.stopPropagation();
    testWorkflow(workflow.id);
  });
  
  card.querySelector('.delete-btn').addEventListener('click', (e) => {
    e.stopPropagation();
    deleteWorkflow(workflow.id);
  });
  
  // Make the entire card clickable to open the editor
  card.addEventListener('click', () => {
    openWorkflowEditor(workflow.id);
  });
  
  col.appendChild(card);
  return col;
}

/**
 * Creates a new workflow and opens the editor
 */
function createNewWorkflow() {
  const workflowId = 'workflow_' + Date.now();
  openWorkflowEditor(workflowId, true);
}

/**
 * Opens the workflow editor for a specific workflow
 * @param {string} workflowId - The ID of the workflow to edit
 * @param {boolean} isNew - Whether this is a new workflow
 */
function openWorkflowEditor(workflowId, isNew = false) {
  window.location.href = `workflow-editor.html?id=${workflowId}&new=${isNew}`;
}

/**
 * Tests a workflow by starting its execution
 * @param {string} workflowId - The ID of the workflow to test
 */
async function testWorkflow(workflowId) {
  try {
    const response = await fetch(`/api/workflows/${workflowId}/start`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    });

    const executionId = await response.text();
    alert(`Workflow execution started. Execution ID: ${executionId}`);

    // Poll for status
    pollWorkflowStatus(workflowId, executionId);
  } catch (error) {
    console.error('Error testing workflow:', error);
    alert('Failed to test workflow');
  }
}

/**
 * Polls the server for the status of a workflow execution
 * @param {string} workflowId - The ID of the workflow
 * @param {string} executionId - The ID of the execution
 */
async function pollWorkflowStatus(workflowId, executionId) {
  const pollInterval = setInterval(async () => {
    try {
      const response = await fetch(`/api/workflows/${workflowId}/status/${executionId}`);
      const status = await response.text();

      if (status === 'COMPLETED' || status === 'FAILED') {
        clearInterval(pollInterval);
        alert(`Workflow execution ${status.toLowerCase()}`);
      }
    } catch (error) {
      console.error('Error polling workflow status:', error);
      clearInterval(pollInterval);
    }
  }, 1000);
}

/**
 * Deletes a workflow
 * @param {string} workflowId - The ID of the workflow to delete
 */
async function deleteWorkflow(workflowId) {
  if (!confirm('Are you sure you want to delete this workflow?')) {
    return;
  }

  try {
    const response = await fetch(`/api/workflows/${workflowId}`, {
      method: 'DELETE'
    });

    if (response.ok) {
      alert('Workflow deleted successfully');
      loadWorkflowList();
    } else {
      throw new Error('Failed to delete workflow');
    }
  } catch (error) {
    console.error('Error deleting workflow:', error);
    alert('Failed to delete workflow');
  }
} 
/**
 * Workflow Editor Main
 * Main entry point for the workflow editor application
 */

// Event handlers
let contextMenuPosition = { x: 0, y: 0 };

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
  // Initialize all modules
  WorkflowState.init();
  CanvasManager.init();
  NodeManager.init();
  ConnectionManager.init();
  WorkflowRenderer.init();
  
  // Get workflow ID from URL parameters
  const urlParams = new URLSearchParams(window.location.search);
  const workflowId = urlParams.get('id');
  const isNew = urlParams.get('new') === 'true';

  if (workflowId) {
    if (isNew) {
      // Create a new workflow with the given ID
      createNewWorkflow(workflowId);
    } else {
      // Load an existing workflow
      loadWorkflow(workflowId);
    }
  } else {
    // No workflow ID provided, redirect to the list page
    window.location.href = 'index.html';
  }

  setupEventListeners();
  
  // Show canvas controls help
  CanvasManager.showControlsHelp();
});

/**
 * Sets up all event listeners for the editor
 */
function setupEventListeners() {
  // Workflow management buttons
  document.getElementById('saveWorkflowBtn').addEventListener('click', saveWorkflow);
  document.getElementById('deleteWorkflowBtn').addEventListener('click', deleteWorkflow);
  document.getElementById('testWorkflowBtn').addEventListener('click', testWorkflow);

  // Canvas mouse events
  const canvas = document.getElementById('workflowCanvas');
  canvas.addEventListener('mousedown', handleCanvasMouseDown);
  canvas.addEventListener('mousemove', handleCanvasMouseMove);
  canvas.addEventListener('mouseup', handleCanvasMouseUp);
  canvas.addEventListener('contextmenu', handleContextMenu);
  canvas.addEventListener('click', handleCanvasClick);
  
  // Canvas wheel event for zooming
  canvas.addEventListener('wheel', handleCanvasWheel, { passive: false });
  
  // Keyboard shortcuts
  document.addEventListener('keydown', handleKeyDown);

  // Context menu items
  const contextMenuItems = document.querySelectorAll('.context-menu-item');
  contextMenuItems.forEach(item => {
    item.addEventListener('click', handleContextMenuItemClick);
  });

  // Node editor panel
  document.getElementById('nodeEditorClose').addEventListener('click', NodeManager.closeNodeEditor.bind(NodeManager));
  document.getElementById('saveNodeBtn').addEventListener('click', NodeManager.saveNodeEdit.bind(NodeManager));

  // Auto-save node edit inputs
  document.getElementById('nodeNameInput').addEventListener('change', NodeManager.autoSaveNodeEdit.bind(NodeManager));
  document.getElementById('nodeParametersInput').addEventListener('change', NodeManager.autoSaveNodeEdit.bind(NodeManager));
  
  // Close context menu when clicking outside
  document.addEventListener('click', (event) => {
    const contextMenu = document.getElementById('contextMenu');
    if (!contextMenu.contains(event.target)) {
      contextMenu.style.display = 'none';
    }
  });
}

/**
 * Handles right-click context menu on the canvas
 * @param {MouseEvent} event - The mouse event
 */
function handleContextMenu(event) {
  event.preventDefault();
  
  // Calculate position considering the current scale and offset
  contextMenuPosition = CanvasManager.screenToCanvasCoordinates(event.clientX, event.clientY);
  
  // Show the context menu at the cursor position
  const contextMenu = document.getElementById('contextMenu');
  contextMenu.style.display = 'block';
  contextMenu.style.left = `${event.clientX}px`;
  contextMenu.style.top = `${event.clientY}px`;
}

/**
 * Handles clicking on a context menu item
 * @param {MouseEvent} event - The mouse event
 */
function handleContextMenuItemClick(event) {
  const nodeType = event.currentTarget.dataset.type;
  NodeManager.createNode(nodeType, contextMenuPosition.x, contextMenuPosition.y);
  
  // Hide the context menu
  document.getElementById('contextMenu').style.display = 'none';
  
  // Render the workflow
  WorkflowRenderer.renderWorkflow();
}

/**
 * Handles clicking on the canvas
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasClick(event) {
  // If clicking on the canvas background (not a node), close the node editor
  if (event.target.id === 'workflowCanvas') {
    NodeManager.closeNodeEditor();
  }
}

/**
 * Handles mouse down events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseDown(event) {
  // If middle mouse button or space key is held while clicking, start panning
  if (event.button === 1 || (event.button === 0 && event.getModifierState('Space'))) {
    CanvasManager.startPanning(event.clientX, event.clientY);
    event.preventDefault();
    return;
  }
  
  const node = event.target.closest('.node');
  if (node) {
    NodeManager.startDragging(node, event.clientX, event.clientY);
    event.preventDefault();
  }
}

/**
 * Handles mouse move events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseMove(event) {
  // Handle canvas panning
  if (CanvasManager.isPanning) {
    CanvasManager.updatePanning(event.clientX, event.clientY);
    event.preventDefault();
    return;
  }
  
  // Handle node dragging
  if (NodeManager.isDragging) {
    const node = event.target.closest('.node');
    if (node) {
      NodeManager.updateDragging(node, event.clientX, event.clientY);
      
      // Update connections
      WorkflowRenderer.updateWorkflow();
    }
  }
}

/**
 * Handles mouse up events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseUp(event) {
  // Handle end of canvas panning
  if (CanvasManager.isPanning) {
    CanvasManager.endPanning();
    event.preventDefault();
    return;
  }
  
  // Handle end of node dragging
  if (NodeManager.isDragging) {
    const node = event.target.closest('.node');
    if (node) {
      NodeManager.endDragging(node);
    }
  }
}

/**
 * Handles mouse wheel events on the canvas for zooming
 * @param {WheelEvent} event - The wheel event
 */
function handleCanvasWheel(event) {
  CanvasManager.handleWheel(event);
}

/**
 * Handles keyboard events for shortcuts
 * @param {KeyboardEvent} event - The keyboard event
 */
function handleKeyDown(event) {
  // 'R' key to reset zoom and pan
  if (event.key === 'r' || event.key === 'R') {
    CanvasManager.resetTransform();
  }
}

/**
 * Creates a new workflow
 * @param {string} workflowId - The ID for the new workflow
 */
function createNewWorkflow(workflowId) {
  WorkflowState.resetWorkflow();
  WorkflowState.currentWorkflow.id = workflowId || 'workflow_' + Date.now();

  document.getElementById('workflowName').value = '';
  document.getElementById('workflowDescription').value = '';
  
  // Initialize history
  WorkflowHistory.initWorkflowHistory(WorkflowState.currentWorkflow);
  
  // Render the empty workflow
  WorkflowRenderer.renderWorkflow();
}

/**
 * Loads a workflow from the server
 * @param {string} workflowId - The ID of the workflow to load
 */
async function loadWorkflow(workflowId) {
  try {
    // Use the API service to fetch the workflow
    const workflow = await WorkflowAPI.fetchWorkflow(workflowId);
    
    // Set the workflow in the state
    WorkflowState.setWorkflow(workflow);

    // Update UI
    document.getElementById('workflowName').value = workflow.name;
    document.getElementById('workflowDescription').value = workflow.description;

    // Render the workflow
    WorkflowRenderer.renderWorkflow();
    
    // Initialize history with loaded workflow
    WorkflowHistory.initWorkflowHistory(WorkflowState.currentWorkflow);
  } catch (error) {
    console.error('Error loading workflow:', error);
    alert('Failed to load workflow: ' + error.message);
    window.location.href = 'index.html';
  }
}

/**
 * Saves the current workflow to the server
 */
async function saveWorkflow() {
  // Update workflow name and description from UI
  WorkflowState.currentWorkflow.name = document.getElementById('workflowName').value;
  WorkflowState.currentWorkflow.description = document.getElementById('workflowDescription').value;

  try {
    // Use the API service to save the workflow
    await WorkflowAPI.saveWorkflow(WorkflowState.currentWorkflow);
    alert('工作流已保存');
    
    // Update history after saving
    WorkflowHistory.forceHistoryUpdate(WorkflowState.currentWorkflow);
  } catch (error) {
    console.error('Error saving workflow:', error);
    alert('保存工作流失败: ' + error.message);
  }
}

/**
 * Deletes the current workflow
 */
async function deleteWorkflow() {
  if (!WorkflowState.currentWorkflow.id) {
    alert('未选择工作流');
    return;
  }

  if (!confirm('确定要删除此工作流吗？')) {
    return;
  }

  try {
    // Use the API service to delete the workflow
    await WorkflowAPI.deleteWorkflow(WorkflowState.currentWorkflow.id);
    alert('Workflow deleted successfully');
    window.location.href = 'index.html';
  } catch (error) {
    console.error('Error deleting workflow:', error);
    alert('删除工作流失败: ' + error.message);
  }
}

/**
 * Tests the current workflow
 */
async function testWorkflow() {
  if (!WorkflowState.currentWorkflow.id) {
    alert('请先保存工作流');
    return;
  }

  try {
    // Use the API service to start the workflow execution
    const executionId = await WorkflowAPI.startWorkflowExecution(WorkflowState.currentWorkflow.id, {});
    alert(`工作流执行已启动。执行ID: ${executionId}`);

    // Poll for status
    pollWorkflowStatus(WorkflowState.currentWorkflow.id, executionId);
  } catch (error) {
    console.error('Error testing workflow:', error);
    alert('测试工作流失败: ' + error.message);
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
      // Use the API service to get the execution status
      const status = await WorkflowAPI.getWorkflowExecutionStatus(workflowId, executionId);

      if (status === 'COMPLETED' || status === 'FAILED') {
        clearInterval(pollInterval);
        alert(`工作流执行${status.toLowerCase()}`);
      }
    } catch (error) {
      console.error('Error polling workflow status:', error);
      clearInterval(pollInterval);
    }
  }, 1000);
} 
/**
 * Workflow Editor
 * Handles the workflow editor interface and functionality
 */

// Workflow editor state
let currentWorkflow = {
  id: null,
  name: '',
  description: '',
  nodes: [],
  startNodeId: null,
  globalVariables: {},
  isActive: true
};

let nodeIdCounter = 1;
let currentEditingNode = null;
let isDragging = false;
let dragOffset = { x: 0, y: 0 };
let contextMenuPosition = { x: 0, y: 0 };
let dragStartTime = 0;
let dragStartPosition = { x: 0, y: 0 };
let isClickEvent = false;

// Constants
const DRAG_THRESHOLD = 5; // Pixels to move before considered a drag
const CLICK_TIMEOUT = 200; // Milliseconds to consider a click vs drag

// Initialize the editor
document.addEventListener('DOMContentLoaded', () => {
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

  // Context menu items
  const contextMenuItems = document.querySelectorAll('.context-menu-item');
  contextMenuItems.forEach(item => {
    item.addEventListener('click', handleContextMenuItemClick);
  });

  // Node editor panel
  document.getElementById('nodeEditorClose').addEventListener('click', closeNodeEditor);
  document.getElementById('saveNodeBtn').addEventListener('click', saveNodeEdit);

  // Auto-save node edit inputs
  document.getElementById('nodeNameInput').addEventListener('change', autoSaveNodeEdit);
  document.getElementById('nodeParametersInput').addEventListener('change', autoSaveNodeEdit);
  
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
  
  const canvas = document.getElementById('workflowCanvas');
  const canvasRect = canvas.getBoundingClientRect();
  
  // Calculate position relative to the canvas
  contextMenuPosition = {
    x: event.clientX - canvasRect.left,
    y: event.clientY - canvasRect.top
  };
  
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
  createNode(nodeType, contextMenuPosition.x, contextMenuPosition.y);
  
  // Hide the context menu
  document.getElementById('contextMenu').style.display = 'none';
}

/**
 * Handles clicking on the canvas
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasClick(event) {
  // If clicking on the canvas background (not a node), close the node editor
  if (event.target.id === 'workflowCanvas') {
    closeNodeEditor();
  }
}

/**
 * Handles mouse down events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseDown(event) {
  const node = event.target.closest('.node');
  if (node) {
    // Record start time and position for click vs drag detection
    dragStartTime = Date.now();
    dragStartPosition = {
      x: event.clientX,
      y: event.clientY
    };
    
    isDragging = true;
    isClickEvent = true; // Assume it's a click until proven otherwise
    
    const nodeRect = node.getBoundingClientRect();
    dragOffset = {
      x: event.clientX - nodeRect.left,
      y: event.clientY - nodeRect.top
    };
    
    node.style.cursor = 'grabbing';
    event.preventDefault();
  }
}

/**
 * Handles mouse move events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseMove(event) {
  if (!isDragging) return;
  
  const node = event.target.closest('.node');
  if (node) {
    // Check if we've moved enough to consider this a drag
    const dx = Math.abs(event.clientX - dragStartPosition.x);
    const dy = Math.abs(event.clientY - dragStartPosition.y);
    
    if (dx > DRAG_THRESHOLD || dy > DRAG_THRESHOLD) {
      isClickEvent = false;
    }
    
    const canvas = document.getElementById('workflowCanvas');
    const canvasRect = canvas.getBoundingClientRect();
    
    const x = event.clientX - canvasRect.left - dragOffset.x;
    const y = event.clientY - canvasRect.top - dragOffset.y;
    
    node.style.left = `${Math.max(0, x)}px`;
    node.style.top = `${Math.max(0, y)}px`;
    
    // Update node position in workflow data
    const nodeId = node.dataset.nodeId;
    const workflowNode = currentWorkflow.nodes.find(n => n.id === nodeId);
    if (workflowNode) {
      workflowNode.position = { x: Math.max(0, x), y: Math.max(0, y) };
      
      // Get the SVG layer and update connections
      const connectionsLayer = document.querySelector('#workflowCanvas > svg');
      if (connectionsLayer) {
        renderConnections(connectionsLayer);
      }
    }
  }
}

/**
 * Handles mouse up events on the canvas for node dragging
 * @param {MouseEvent} event - The mouse event
 */
function handleCanvasMouseUp(event) {
  if (isDragging) {
    const node = event.target.closest('.node');
    if (node) {
      node.style.cursor = 'grab';
      
      // Check if this was a click (short duration and minimal movement)
      const duration = Date.now() - dragStartTime;
      
      if (isClickEvent && duration < CLICK_TIMEOUT) {
        // This was a click, open the node editor
        const nodeId = node.dataset.nodeId;
        const workflowNode = currentWorkflow.nodes.find(n => n.id === nodeId);
        if (workflowNode) {
          openNodeEditor(workflowNode);
        }
      } else {
        // This was a drag, update history
        forceHistoryUpdate(currentWorkflow);
      }
    }
    
    isDragging = false;
    isClickEvent = false;
  }
}

/**
 * Creates a new node in the workflow
 * @param {string} type - The type of node to create
 * @param {number} x - The x position of the node
 * @param {number} y - The y position of the node
 */
function createNode(type, x, y) {
  const nodeId = `node_${nodeIdCounter++}`;
  const node = {
    id: nodeId,
    name: `${type} Node`,
    type: type,
    parameters: {},
    nextNodes: {},
    position: {
      x: Math.max(0, x),
      y: Math.max(0, y)
    }
  };

  currentWorkflow.nodes.push(node);
  if (type === 'START') {
    currentWorkflow.startNodeId = nodeId;
  }

  renderWorkflow();
  
  // Update history
  forceHistoryUpdate(currentWorkflow);
}

/**
 * Renders the entire workflow on the canvas
 */
function renderWorkflow() {
  const canvas = document.getElementById('workflowCanvas');
  canvas.innerHTML = '';

  // Create SVG layer for connections
  const connectionsLayer = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  connectionsLayer.style.position = 'absolute';
  connectionsLayer.style.top = '0';
  connectionsLayer.style.left = '0';
  connectionsLayer.style.width = '100%';
  connectionsLayer.style.height = '100%';
  connectionsLayer.style.pointerEvents = 'none';
  canvas.appendChild(connectionsLayer);

  // Render nodes
  currentWorkflow.nodes.forEach(node => {
    const nodeElement = document.createElement('div');
    nodeElement.className = `node node-${node.type.toLowerCase()}`;
    nodeElement.dataset.nodeId = node.id;
    nodeElement.innerHTML = `
      <div class="node-header">${node.name}</div>
      <div class="node-type">${node.type}</div>
      <div class="connection-point input"></div>
      <div class="connection-point output"></div>
    `;
    
    nodeElement.style.left = `${node.position.x}px`;
    nodeElement.style.top = `${node.position.y}px`;
    nodeElement.style.cursor = 'grab';
    
    canvas.appendChild(nodeElement);
  });

  renderConnections(connectionsLayer);
}

/**
 * Renders the connections between nodes
 * @param {SVGElement} svg - The SVG element to render connections in
 */
function renderConnections(svg) {
  if (!svg) return;
  svg.innerHTML = '';
  
  currentWorkflow.nodes.forEach(sourceNode => {
    if (sourceNode.nextNodes) {
      Object.entries(sourceNode.nextNodes).forEach(([condition, targetId]) => {
        const targetNode = currentWorkflow.nodes.find(n => n.id === targetId);
        if (targetNode) {
          drawConnection(svg, sourceNode, targetNode, condition);
        }
      });
    }
  });
}

/**
 * Draws a connection between two nodes
 * @param {SVGElement} svg - The SVG element to draw in
 * @param {Object} sourceNode - The source node
 * @param {Object} targetNode - The target node
 * @param {string} condition - The condition for the connection
 */
function drawConnection(svg, sourceNode, targetNode, condition) {
  const sourceElement = document.querySelector(`[data-node-id="${sourceNode.id}"]`);
  const targetElement = document.querySelector(`[data-node-id="${targetNode.id}"]`);
  
  if (!sourceElement || !targetElement) return;
  
  // Get the output and input connection points
  const outputPoint = sourceElement.querySelector('.connection-point.output');
  const inputPoint = targetElement.querySelector('.connection-point.input');
  
  if (!outputPoint || !inputPoint) return;
  
  // Calculate the absolute positions of the connection points
  const sourceRect = outputPoint.getBoundingClientRect();
  const targetRect = inputPoint.getBoundingClientRect();
  const canvasRect = svg.getBoundingClientRect();
  
  const sourceX = sourceNode.position.x + sourceElement.offsetWidth;
  const sourceY = sourceNode.position.y + (sourceElement.offsetHeight / 2);
  const targetX = targetNode.position.x;
  const targetY = targetNode.position.y + (targetElement.offsetHeight / 2);
  
  // Calculate control points for the curve
  const dx = Math.abs(targetX - sourceX);
  const controlPointOffset = Math.min(80, dx / 2);
  const controlPoint1X = sourceX + controlPointOffset;
  const controlPoint2X = targetX - controlPointOffset;
  
  // Create path
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  const d = `M ${sourceX} ${sourceY} 
             C ${controlPoint1X} ${sourceY},
               ${controlPoint2X} ${targetY},
               ${targetX} ${targetY}`;
  
  path.setAttribute('d', d);
  path.setAttribute('stroke', getConnectionColor(sourceNode.type));
  path.setAttribute('stroke-width', '2');
  path.setAttribute('fill', 'none');
  
  // Add arrow
  const arrowSize = 10;
  const angle = Math.atan2(targetY - sourceY, targetX - sourceX);
  const arrowX = targetX - arrowSize * Math.cos(angle);
  const arrowY = targetY - arrowSize * Math.sin(angle);
  
  const arrow = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  arrow.setAttribute('d', `
    M ${arrowX} ${arrowY}
    L ${targetX} ${targetY}
    L ${arrowX - arrowSize * Math.cos(angle - Math.PI/6)} ${arrowY - arrowSize * Math.sin(angle - Math.PI/6)}
  `);
  arrow.setAttribute('fill', getConnectionColor(sourceNode.type));
  
  // Add condition label if needed
  if (condition && condition !== 'default') {
    const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    const midX = (sourceX + targetX) / 2;
    const midY = (sourceY + targetY) / 2 - 10;
    
    text.setAttribute('x', midX.toString());
    text.setAttribute('y', midY.toString());
    text.setAttribute('text-anchor', 'middle');
    text.setAttribute('fill', '#666');
    text.textContent = condition;
    
    svg.appendChild(text);
  }
  
  svg.appendChild(path);
  svg.appendChild(arrow);
}

/**
 * Creates a new workflow
 * @param {string} workflowId - The ID for the new workflow
 */
function createNewWorkflow(workflowId) {
  currentWorkflow = {
    id: workflowId || 'workflow_' + Date.now(),
    name: '',
    description: '',
    nodes: [],
    startNodeId: null,
    globalVariables: {},
    isActive: true
  };

  document.getElementById('workflowName').value = '';
  document.getElementById('workflowDescription').value = '';
  document.getElementById('workflowCanvas').innerHTML = '';
  nodeIdCounter = 1;
  
  // Initialize history
  initWorkflowHistory(currentWorkflow);
}

/**
 * Loads a workflow from the server
 * @param {string} workflowId - The ID of the workflow to load
 */
async function loadWorkflow(workflowId) {
  try {
    // Use the API service to fetch the workflow
    currentWorkflow = await fetchWorkflow(workflowId);

    document.getElementById('workflowName').value = currentWorkflow.name;
    document.getElementById('workflowDescription').value = currentWorkflow.description;

    // Find the highest node ID to set the counter
    let maxId = 0;
    currentWorkflow.nodes.forEach(node => {
      const idMatch = node.id.match(/node_(\d+)/);
      if (idMatch) {
        const id = parseInt(idMatch[1]);
        if (id > maxId) {
          maxId = id;
        }
      }
    });
    nodeIdCounter = maxId + 1;

    renderWorkflow();
    
    // Initialize history with loaded workflow
    initWorkflowHistory(currentWorkflow);
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
  currentWorkflow.name = document.getElementById('workflowName').value;
  currentWorkflow.description = document.getElementById('workflowDescription').value;

  try {
    // Use the API service to save the workflow
    await saveWorkflowToServer(currentWorkflow);
    alert('Workflow saved successfully');
    
    // Update history after saving
    forceHistoryUpdate(currentWorkflow);
  } catch (error) {
    console.error('Error saving workflow:', error);
    alert('Failed to save workflow: ' + error.message);
  }
}

/**
 * Deletes the current workflow
 */
async function deleteWorkflow() {
  if (!currentWorkflow.id) {
    alert('No workflow selected');
    return;
  }

  if (!confirm('Are you sure you want to delete this workflow?')) {
    return;
  }

  try {
    // Use the API service to delete the workflow
    await deleteWorkflowFromServer(currentWorkflow.id);
    alert('Workflow deleted successfully');
    window.location.href = 'index.html';
  } catch (error) {
    console.error('Error deleting workflow:', error);
    alert('Failed to delete workflow: ' + error.message);
  }
}

/**
 * Tests the current workflow
 */
async function testWorkflow() {
  if (!currentWorkflow.id) {
    alert('Please save the workflow first');
    return;
  }

  try {
    // Use the API service to start the workflow execution
    const executionId = await startWorkflowExecution(currentWorkflow.id, {});
    alert(`Workflow execution started. Execution ID: ${executionId}`);

    // Poll for status
    pollWorkflowStatus(currentWorkflow.id, executionId);
  } catch (error) {
    console.error('Error testing workflow:', error);
    alert('Failed to test workflow: ' + error.message);
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
      const status = await getWorkflowExecutionStatus(workflowId, executionId);

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
 * Opens the node editor panel for a node
 * @param {Object} node - The node to edit
 */
function openNodeEditor(node) {
  currentEditingNode = node;

  const form = document.getElementById('nodeEditForm');
  form.reset();

  document.getElementById('nodeNameInput').value = node.name;
  document.getElementById('nodeParametersInput').value = JSON.stringify(node.parameters, null, 2);

  const connectionsDiv = document.getElementById('nodeConnectionsInputs');
  connectionsDiv.innerHTML = '';

  if (node.type === 'CONDITION') {
    addConnectionInput(connectionsDiv, 'true', node.nextNodes['true'] || '');
    addConnectionInput(connectionsDiv, 'false', node.nextNodes['false'] || '');
  } else if (node.type !== 'END') {
    addConnectionInput(connectionsDiv, 'default', node.nextNodes['default'] || '');
  }

  // Update the editor title
  document.getElementById('nodeEditorTitle').textContent = `Edit ${node.type} Node`;

  // Show the editor panel
  document.getElementById('nodeEditorPanel').classList.add('visible');
  
  // Add event listeners for connection selects
  setTimeout(() => {
    const connectionSelects = document.querySelectorAll('#nodeConnectionsInputs select');
    connectionSelects.forEach(select => {
      select.addEventListener('change', autoSaveNodeEdit);
    });
  }, 0);
}

/**
 * Closes the node editor panel
 */
function closeNodeEditor() {
  document.getElementById('nodeEditorPanel').classList.remove('visible');
  currentEditingNode = null;
}

/**
 * Adds a connection input to the node editor
 * @param {HTMLElement} container - The container to add the input to
 * @param {string} label - The label for the input
 * @param {string} value - The current value of the input
 */
function addConnectionInput(container, label, value) {
  const div = document.createElement('div');
  div.className = 'mb-2';
  
  const selectId = `connection_${label}`;
  div.innerHTML = `
    <label class="form-label" for="${selectId}">Next node for "${label}"</label>
    <select class="form-control" id="${selectId}" data-connection="${label}">
      <option value="">Select node</option>
      ${currentWorkflow.nodes
        .filter(n => n.id !== currentEditingNode.id)
        .map(n => `<option value="${n.id}" ${value === n.id ? 'selected' : ''}>${n.name}</option>`)
        .join('')}
    </select>
  `;
  container.appendChild(div);
}

/**
 * Auto-saves the node edit when inputs change
 */
function autoSaveNodeEdit() {
  if (!currentEditingNode) return;
  
  // Update node name
  currentEditingNode.name = document.getElementById('nodeNameInput').value;

  // Update parameters
  try {
    const paramsText = document.getElementById('nodeParametersInput').value;
    currentEditingNode.parameters = paramsText ? JSON.parse(paramsText) : {};
  } catch (error) {
    console.error('Invalid JSON in parameters:', error);
    // Don't update parameters if JSON is invalid
    return;
  }

  // Update connections
  currentEditingNode.nextNodes = {};
  const connections = document.querySelectorAll('#nodeConnectionsInputs select');
  connections.forEach(select => {
    if (select.value) {
      currentEditingNode.nextNodes[select.dataset.connection] = select.value;
    }
  });

  // Re-render the workflow
  renderWorkflow();
  
  // Update history
  forceHistoryUpdate(currentWorkflow);
}

/**
 * Saves the changes made in the node editor
 */
function saveNodeEdit() {
  autoSaveNodeEdit();
  closeNodeEditor();
}

/**
 * Gets the color for a connection based on the node type
 * @param {string} nodeType - The type of node
 * @returns {string} - The color for the connection
 */
function getConnectionColor(nodeType) {
  switch (nodeType) {
    case 'START': return '#28a745';
    case 'CONDITION': return '#ffc107';
    case 'FUNCTION': return '#17a2b8';
    case 'END': return '#dc3545';
    default: return '#6c757d';
  }
} 
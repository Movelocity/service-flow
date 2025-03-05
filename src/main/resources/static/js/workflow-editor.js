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
const nodeEditModal = new bootstrap.Modal(document.getElementById('nodeEditModal'));
let currentEditingNode = null;
let isDragging = false;
let dragOffset = { x: 0, y: 0 };

// Initialize the editor
document.addEventListener('DOMContentLoaded', () => {
  loadWorkflowList();
  setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
  // Workflow management buttons
  document.getElementById('newWorkflowBtn').addEventListener('click', createNewWorkflow);
  document.getElementById('saveWorkflowBtn').addEventListener('click', saveWorkflow);
  document.getElementById('deleteWorkflowBtn').addEventListener('click', deleteWorkflow);
  document.getElementById('testWorkflowBtn').addEventListener('click', testWorkflow);

  // Sidebar toggle
  document.getElementById('sidebarToggle').addEventListener('click', toggleSidebar);

  // Node type drag and drop
  const nodeTypes = document.querySelectorAll('.node-item');
  nodeTypes.forEach(node => {
    node.addEventListener('dragstart', handleDragStart);
    node.setAttribute('draggable', 'true');
  });

  const canvas = document.getElementById('workflowCanvas');
  canvas.addEventListener('dragover', handleDragOver);
  canvas.addEventListener('drop', handleDrop);

  // Node edit modal
  document.getElementById('saveNodeBtn').addEventListener('click', saveNodeEdit);

  // Canvas mouse events for node dragging
  canvas.addEventListener('mousedown', handleCanvasMouseDown);
  document.addEventListener('mousemove', handleCanvasMouseMove);
  document.addEventListener('mouseup', handleCanvasMouseUp);
}

// Sidebar toggle
function toggleSidebar() {
  const sidebar = document.getElementById('sidebar');
  const mainContent = document.getElementById('mainContent');
  const toggleButton = document.getElementById('sidebarToggle');
  
  sidebar.classList.toggle('collapsed');
  mainContent.classList.toggle('expanded');
  toggleButton.innerHTML = sidebar.classList.contains('collapsed') ? 
    '<i class="bi bi-chevron-right"></i>' : 
    '<i class="bi bi-chevron-left"></i>';
}

// Node dragging
function handleCanvasMouseDown(event) {
  const node = event.target.closest('.node');
  if (node) {
    isDragging = true;
    const nodeRect = node.getBoundingClientRect();
    dragOffset = {
      x: event.clientX - nodeRect.left,
      y: event.clientY - nodeRect.top
    };
    node.style.cursor = 'grabbing';
    event.preventDefault();
  }
}

function handleCanvasMouseMove(event) {
  if (!isDragging) return;
  
  const node = event.target.closest('.node');
  if (node) {
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
    }
    
    renderConnections();
  }
}

function handleCanvasMouseUp(event) {
  if (isDragging) {
    const node = event.target.closest('.node');
    if (node) {
      node.style.cursor = 'grab';
    }
    isDragging = false;
  }
}

// Node type drag and drop
function handleDragStart(event) {
  event.dataTransfer.setData('nodeType', event.target.dataset.type);
  event.dataTransfer.effectAllowed = 'copy';
}

function handleDragOver(event) {
  event.preventDefault();
  event.dataTransfer.dropEffect = 'copy';
}

function handleDrop(event) {
  event.preventDefault();
  const nodeType = event.dataTransfer.getData('nodeType');
  const canvas = document.getElementById('workflowCanvas');
  const canvasRect = canvas.getBoundingClientRect();
  
  const x = event.clientX - canvasRect.left;
  const y = event.clientY - canvasRect.top;
  
  createNode(nodeType, x, y);
}

// Node creation and rendering
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
}

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
    
    nodeElement.addEventListener('click', () => openNodeEditor(node));
    canvas.appendChild(nodeElement);
  });

  renderConnections(connectionsLayer);
}

function renderConnections(svg) {
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

function drawConnection(svg, sourceNode, targetNode, condition) {
  const sourceElement = document.querySelector(`[data-node-id="${sourceNode.id}"]`);
  const targetElement = document.querySelector(`[data-node-id="${targetId}"]`);
  
  if (!sourceElement || !targetElement) return;
  
  const sourceRect = sourceElement.getBoundingClientRect();
  const targetRect = targetElement.getBoundingClientRect();
  const canvas = document.getElementById('workflowCanvas');
  const canvasRect = canvas.getBoundingClientRect();
  
  const sourceX = sourceNode.position.x + sourceElement.offsetWidth;
  const sourceY = sourceNode.position.y + sourceElement.offsetHeight / 2;
  const targetX = targetNode.position.x;
  const targetY = targetNode.position.y + targetElement.offsetHeight / 2;
  
  // Create path
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  const controlPoint1X = sourceX + Math.min(100, (targetX - sourceX) / 2);
  const controlPoint2X = targetX - Math.min(100, (targetX - sourceX) / 2);
  
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

// Workflow list management
async function loadWorkflowList() {
  try {
    const response = await fetch('/api/workflows');
    const workflows = await response.json();

    const workflowList = document.getElementById('workflowList');
    workflowList.innerHTML = '';

    workflows.forEach(workflowId => {
      const workflowItem = document.createElement('div');
      workflowItem.className = 'node-item';
      workflowItem.textContent = workflowId;
      workflowItem.addEventListener('click', () => loadWorkflow(workflowId));
      workflowList.appendChild(workflowItem);
    });
  } catch (error) {
    console.error('Error loading workflow list:', error);
    alert('Failed to load workflow list');
  }
}

// Workflow management
function createNewWorkflow() {
  currentWorkflow = {
    id: 'workflow_' + Date.now(),
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
}

async function loadWorkflow(workflowId) {
  try {
    const response = await fetch(`/api/workflows/${workflowId}`);
    currentWorkflow = await response.json();

    document.getElementById('workflowName').value = currentWorkflow.name;
    document.getElementById('workflowDescription').value = currentWorkflow.description;

    renderWorkflow();
  } catch (error) {
    console.error('Error loading workflow:', error);
    alert('Failed to load workflow');
  }
}

async function saveWorkflow() {
  currentWorkflow.name = document.getElementById('workflowName').value;
  currentWorkflow.description = document.getElementById('workflowDescription').value;

  try {
    const response = await fetch('/api/workflows', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(currentWorkflow)
    });

    if (response.ok) {
      alert('Workflow saved successfully');
      loadWorkflowList();
    } else {
      throw new Error('Failed to save workflow');
    }
  } catch (error) {
    console.error('Error saving workflow:', error);
    alert('Failed to save workflow');
  }
}

async function deleteWorkflow() {
  if (!currentWorkflow.id) {
    alert('No workflow selected');
    return;
  }

  if (!confirm('Are you sure you want to delete this workflow?')) {
    return;
  }

  try {
    const response = await fetch(`/api/workflows/${currentWorkflow.id}`, {
      method: 'DELETE'
    });

    if (response.ok) {
      alert('Workflow deleted successfully');
      createNewWorkflow();
      loadWorkflowList();
    } else {
      throw new Error('Failed to delete workflow');
    }
  } catch (error) {
    console.error('Error deleting workflow:', error);
    alert('Failed to delete workflow');
  }
}

async function testWorkflow() {
  if (!currentWorkflow.id) {
    alert('Please save the workflow first');
    return;
  }

  try {
    const response = await fetch(`/api/workflows/${currentWorkflow.id}/start`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    });

    const executionId = await response.text();
    alert(`Workflow execution started. Execution ID: ${executionId}`);

    // Poll for status
    pollWorkflowStatus(currentWorkflow.id, executionId);
  } catch (error) {
    console.error('Error testing workflow:', error);
    alert('Failed to test workflow');
  }
}

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

function openNodeEditor(node) {
  currentEditingNode = node;

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

  nodeEditModal.show();
}

function addConnectionInput(container, label, value) {
  const div = document.createElement('div');
  div.className = 'mb-2';
  div.innerHTML = `
        <label class="form-label">Next node for "${label}"</label>
        <select class="form-control" data-connection="${label}">
            <option value="">Select node</option>
            ${currentWorkflow.nodes
      .filter(n => n.id !== currentEditingNode.id)
      .map(n => `<option value="${n.id}" ${value === n.id ? 'selected' : ''}>${n.name}</option>`)
      .join('')}
        </select>
    `;
  container.appendChild(div);
}

function saveNodeEdit() {
  currentEditingNode.name = document.getElementById('nodeNameInput').value;

  try {
    currentEditingNode.parameters = JSON.parse(document.getElementById('nodeParametersInput').value);
  } catch (error) {
    alert('Invalid JSON in parameters');
    return;
  }

  currentEditingNode.nextNodes = {};
  const connections = document.querySelectorAll('#nodeConnectionsInputs select');
  connections.forEach(select => {
    if (select.value) {
      currentEditingNode.nextNodes[select.dataset.connection] = select.value;
    }
  });

  renderWorkflow();
  nodeEditModal.hide();
}

function getConnectionColor(nodeType) {
  switch (nodeType) {
    case 'START': return '#28a745';
    case 'CONDITION': return '#ffc107';
    case 'FUNCTION': return '#17a2b8';
    case 'END': return '#dc3545';
    default: return '#6c757d';
  }
} 
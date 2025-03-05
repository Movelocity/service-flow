/**
 * Node Manager
 * Handles node creation, editing, and dragging operations
 */

const NodeManager = {
  // Node interaction state
  isDragging: false,
  dragOffset: { x: 0, y: 0 },
  dragStartTime: 0,
  dragStartPosition: { x: 0, y: 0 },
  isClickEvent: false,
  
  // Constants
  DRAG_THRESHOLD: 5, // Pixels to move before considered a drag
  CLICK_TIMEOUT: 200, // Milliseconds to consider a click vs drag
  
  // Initialize the node manager
  init() {
    this.isDragging = false;
    this.isClickEvent = false;
  },
  
  // Create a new node
  createNode(type, x, y) {
    const nodeId = WorkflowState.generateNodeId();
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

    WorkflowState.addNode(node);
    if (type === 'START') {
      WorkflowState.currentWorkflow.startNodeId = nodeId;
    }

    // Update history
    WorkflowHistory.forceHistoryUpdate(WorkflowState.currentWorkflow);
    
    return node;
  },
  
  // Start dragging a node
  startDragging(node, clientX, clientY) {
    // Record start time and position for click vs drag detection
    this.dragStartTime = Date.now();
    this.dragStartPosition = {
      x: clientX,
      y: clientY
    };
    
    this.isDragging = true;
    this.isClickEvent = true; // Assume it's a click until proven otherwise
    
    // Get node position in canvas coordinates
    const nodePosition = {
      x: parseFloat(node.style.left),
      y: parseFloat(node.style.top)
    };
    
    // Calculate the offset in screen coordinates
    const screenPos = CanvasManager.canvasToScreenCoordinates(nodePosition.x, nodePosition.y);
    
    this.dragOffset = {
      x: clientX - screenPos.x,
      y: clientY - screenPos.y
    };
    
    node.style.cursor = 'grabbing';
  },
  
  // Update node position during dragging
  updateDragging(node, clientX, clientY) {
    if (!this.isDragging) return;
    
    // Check if we've moved enough to consider this a drag
    const dx = Math.abs(clientX - this.dragStartPosition.x);
    const dy = Math.abs(clientY - this.dragStartPosition.y);
    
    if (dx > this.DRAG_THRESHOLD || dy > this.DRAG_THRESHOLD) {
      this.isClickEvent = false;
    }
    
    // Calculate position in canvas coordinates
    const canvasCoords = CanvasManager.screenToCanvasCoordinates(
      clientX - this.dragOffset.x, 
      clientY - this.dragOffset.y
    );
    
    const x = Math.max(0, canvasCoords.x);
    const y = Math.max(0, canvasCoords.y);
    
    node.style.left = `${x}px`;
    node.style.top = `${y}px`;
    
    // Update node position in workflow data
    const nodeId = node.dataset.nodeId;
    const workflowNode = WorkflowState.getNodeById(nodeId);
    if (workflowNode) {
      workflowNode.position = { x, y };
    }
  },
  
  // End node dragging
  endDragging(node) {
    if (!this.isDragging) return;
    
    node.style.cursor = 'grab';
    
    // Check if this was a click (short duration and minimal movement)
    const duration = Date.now() - this.dragStartTime;
    
    if (this.isClickEvent && duration < this.CLICK_TIMEOUT) {
      // This was a click, open the node editor
      const nodeId = node.dataset.nodeId;
      const workflowNode = WorkflowState.getNodeById(nodeId);
      if (workflowNode) {
        this.openNodeEditor(workflowNode);
      }
    } else {
      // This was a drag, update history
      WorkflowHistory.forceHistoryUpdate(WorkflowState.currentWorkflow);
    }
    
    this.isDragging = false;
    this.isClickEvent = false;
  },
  
  // Open the node editor panel
  openNodeEditor(node) {
    WorkflowState.setEditingNode(node);

    const form = document.getElementById('nodeEditForm');
    form.reset();

    document.getElementById('nodeNameInput').value = node.name;
    document.getElementById('nodeParametersInput').value = JSON.stringify(node.parameters, null, 2);

    const connectionsDiv = document.getElementById('nodeConnectionsInputs');
    connectionsDiv.innerHTML = '';

    if (node.type === 'CONDITION') {
      this.addConnectionInput(connectionsDiv, 'true', node.nextNodes['true'] || '');
      this.addConnectionInput(connectionsDiv, 'false', node.nextNodes['false'] || '');
    } else if (node.type !== 'END') {
      this.addConnectionInput(connectionsDiv, 'default', node.nextNodes['default'] || '');
    }

    // Update the editor title
    document.getElementById('nodeEditorTitle').textContent = `Edit ${node.type} Node`;

    // Show the editor panel
    document.getElementById('nodeEditorPanel').classList.add('visible');
    
    // Add event listeners for connection selects
    setTimeout(() => {
      const connectionSelects = document.querySelectorAll('#nodeConnectionsInputs select');
      connectionSelects.forEach(select => {
        select.addEventListener('change', this.autoSaveNodeEdit);
      });
    }, 0);
  },
  
  // Close the node editor panel
  closeNodeEditor() {
    document.getElementById('nodeEditorPanel').classList.remove('visible');
    WorkflowState.clearEditingNode();
  },
  
  // Add a connection input to the node editor
  addConnectionInput(container, label, value) {
    const div = document.createElement('div');
    div.className = 'mb-2';
    
    const selectId = `connection_${label}`;
    div.innerHTML = `
      <label class="form-label" for="${selectId}">${label}</label>
      <select class="form-control" id="${selectId}" data-connection="${label}">
        <option value="">Select node</option>
        ${WorkflowState.currentWorkflow.nodes
          .filter(n => n.id !== WorkflowState.currentEditingNode.id)
          .map(n => `<option value="${n.id}" ${value === n.id ? 'selected' : ''}>${n.name}</option>`)
          .join('')}
      </select>
    `;
    container.appendChild(div);
  },
  
  // Auto-save the node edit when inputs change
  autoSaveNodeEdit() {
    if (!WorkflowState.currentEditingNode) return;
    
    // Update node name
    WorkflowState.currentEditingNode.name = document.getElementById('nodeNameInput').value;

    // Update parameters
    try {
      const paramsText = document.getElementById('nodeParametersInput').value;
      WorkflowState.currentEditingNode.parameters = paramsText ? JSON.parse(paramsText) : {};
    } catch (error) {
      console.error('Invalid JSON in parameters:', error);
      // Don't update parameters if JSON is invalid
      return;
    }

    // Update connections
    WorkflowState.currentEditingNode.nextNodes = {};
    const connections = document.querySelectorAll('#nodeConnectionsInputs select');
    connections.forEach(select => {
      if (select.value) {
        WorkflowState.currentEditingNode.nextNodes[select.dataset.connection] = select.value;
      }
    });

    // Re-render the workflow
    WorkflowRenderer.renderWorkflow();
    
    // Update history
    WorkflowHistory.forceHistoryUpdate(WorkflowState.currentWorkflow);
  },
  
  // Save the changes made in the node editor
  saveNodeEdit() {
    this.autoSaveNodeEdit();
    this.closeNodeEditor();
  },
  
  // Get the color for a connection based on the node type
  getConnectionColor(nodeType) {
    switch (nodeType) {
      case 'START': return '#28a745';
      case 'CONDITION': return '#ffc107';
      case 'FUNCTION': return '#17a2b8';
      case 'END': return '#dc3545';
      default: return '#6c757d';
    }
  }
};

// Export the NodeManager object
window.NodeManager = NodeManager; 
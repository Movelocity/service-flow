/**
 * Workflow Renderer
 * Handles rendering the workflow on the canvas
 */

const WorkflowRenderer = {
  // Initialize the renderer
  init() {
    // Nothing to initialize
  },
  
  // Render the entire workflow on the canvas
  renderWorkflow() {
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
    WorkflowState.currentWorkflow.nodes.forEach(node => {
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

    // Render connections
    ConnectionManager.renderConnections(connectionsLayer);
    
    // Apply current transform
    CanvasManager.applyTransform();
  },
  
  // Update the workflow display after changes
  updateWorkflow() {
    // Get the SVG layer and update connections
    const connectionsLayer = document.querySelector('#workflowCanvas > svg');
    if (connectionsLayer) {
      ConnectionManager.renderConnections(connectionsLayer);
    }
  }
};

// Export the WorkflowRenderer object
window.WorkflowRenderer = WorkflowRenderer; 
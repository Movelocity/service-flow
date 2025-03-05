/**
 * Connection Manager
 * Handles drawing and managing connections between nodes
 */

const ConnectionManager = {
  // Initialize the connection manager
  init() {
    // Nothing to initialize
  },
  
  // Render all connections between nodes
  renderConnections(svg) {
    if (!svg) return;
    svg.innerHTML = '';
    
    WorkflowState.currentWorkflow.nodes.forEach(sourceNode => {
      if (sourceNode.nextNodes) {
        Object.entries(sourceNode.nextNodes).forEach(([condition, targetId]) => {
          const targetNode = WorkflowState.getNodeById(targetId);
          if (targetNode) {
            this.drawConnection(svg, sourceNode, targetNode, condition);
          }
        });
      }
    });
  },
  
  // Draw a connection between two nodes
  drawConnection(svg, sourceNode, targetNode, condition) {
    const sourceElement = document.querySelector(`[data-node-id="${sourceNode.id}"]`);
    const targetElement = document.querySelector(`[data-node-id="${targetNode.id}"]`);
    
    if (!sourceElement || !targetElement) return;
    
    // Get the output and input connection points
    const outputPoint = sourceElement.querySelector('.connection-point.output');
    const inputPoint = targetElement.querySelector('.connection-point.input');
    
    if (!outputPoint || !inputPoint) return;
    
    // Use the node positions directly from the workflow data
    // This ensures connections are drawn correctly regardless of zoom level
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
    path.setAttribute('stroke', NodeManager.getConnectionColor(sourceNode.type));
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
    arrow.setAttribute('fill', NodeManager.getConnectionColor(sourceNode.type));
    
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
};

// Export the ConnectionManager object
window.ConnectionManager = ConnectionManager; 
/**
 * Canvas Manager
 * Handles canvas zoom, pan and transformation operations
 */

const CanvasManager = {
  // Canvas state
  scale: 1,
  offset: { x: 0, y: 0 },
  isPanning: false,
  panStartPosition: { x: 0, y: 0 },
  
  // Constants
  MIN_SCALE: 0.5,
  MAX_SCALE: 2.0,
  ZOOM_SENSITIVITY: 0.001,
  
  // Initialize the canvas manager
  init() {
    this.scale = 1;
    this.offset = { x: 0, y: 0 };
    this.isPanning = false;
    this.createZoomInfoDisplay();
  },
  
  // Apply the current transform to the canvas
  applyTransform() {
    const canvas = document.getElementById('workflowCanvas');
    const transform = `translate(${this.offset.x}px, ${this.offset.y}px) scale(${this.scale})`;
    canvas.style.transform = transform;
    canvas.style.transformOrigin = '0 0';
    
    // Update zoom info display
    this.updateZoomInfo();
  },
  
  // Reset the canvas transform to default
  resetTransform() {
    this.scale = 1;
    this.offset = { x: 0, y: 0 };
    this.applyTransform();
  },
  
  // Handle mouse wheel events for zooming
  handleWheel(event) {
    event.preventDefault();
    
    // Calculate zoom factor based on wheel delta
    const delta = event.deltaY || event.detail || event.wheelDelta;
    const zoomFactor = 1 - delta * this.ZOOM_SENSITIVITY;
    
    // Calculate new scale
    const newScale = Math.max(this.MIN_SCALE, Math.min(this.MAX_SCALE, this.scale * zoomFactor));
    
    // Calculate zoom center (mouse position)
    const canvas = document.getElementById('workflowCanvas');
    const canvasRect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - canvasRect.left;
    const mouseY = event.clientY - canvasRect.top;
    
    // Calculate new offset to zoom toward mouse position
    if (newScale !== this.scale) {
      const scaleDiff = newScale - this.scale;
      this.offset.x -= (mouseX - this.offset.x) * (scaleDiff / this.scale);
      this.offset.y -= (mouseY - this.offset.y) * (scaleDiff / this.scale);
      this.scale = newScale;
      
      this.applyTransform();
    }
  },
  
  // Start canvas panning
  startPanning(x, y) {
    this.isPanning = true;
    this.panStartPosition = { x, y };
    document.body.style.cursor = 'grabbing';
  },
  
  // Update canvas panning
  updatePanning(x, y) {
    if (!this.isPanning) return;
    
    const dx = x - this.panStartPosition.x;
    const dy = y - this.panStartPosition.y;
    
    this.offset.x += dx;
    this.offset.y += dy;
    
    this.panStartPosition = { x, y };
    
    this.applyTransform();
  },
  
  // End canvas panning
  endPanning() {
    this.isPanning = false;
    document.body.style.cursor = 'default';
  },
  
  // Convert screen coordinates to canvas coordinates
  screenToCanvasCoordinates(screenX, screenY) {
    const canvas = document.getElementById('workflowCanvas');
    const canvasRect = canvas.getBoundingClientRect();
    
    return {
      x: (screenX - canvasRect.left - this.offset.x) / this.scale,
      y: (screenY - canvasRect.top - this.offset.y) / this.scale
    };
  },
  
  // Convert canvas coordinates to screen coordinates
  canvasToScreenCoordinates(canvasX, canvasY) {
    const canvas = document.getElementById('workflowCanvas');
    const canvasRect = canvas.getBoundingClientRect();
    
    return {
      x: canvasRect.left + (canvasX * this.scale) + this.offset.x,
      y: canvasRect.top + (canvasY * this.scale) + this.offset.y
    };
  },
  
  // Create zoom info display
  createZoomInfoDisplay() {
    const zoomInfo = document.createElement('div');
    zoomInfo.id = 'zoomInfo';
    zoomInfo.style.position = 'absolute';
    zoomInfo.style.bottom = '10px';
    zoomInfo.style.right = '10px';
    zoomInfo.style.background = 'rgba(0, 0, 0, 0.5)';
    zoomInfo.style.color = 'white';
    zoomInfo.style.padding = '5px 10px';
    zoomInfo.style.borderRadius = '3px';
    zoomInfo.style.fontSize = '12px';
    zoomInfo.style.pointerEvents = 'none';
    zoomInfo.textContent = `缩放: ${Math.round(this.scale * 100)}%`;
    
    const container = document.querySelector('.workflow-editor-container');
    if (container) {
      container.appendChild(zoomInfo);
    } else {
      document.getElementById('workflowCanvas').parentNode.appendChild(zoomInfo);
    }
  },
  
  // Update zoom info display
  updateZoomInfo() {
    const zoomInfo = document.getElementById('zoomInfo');
    if (zoomInfo) {
      zoomInfo.textContent = `缩放: ${Math.round(this.scale * 100)}%`;
    } else {
      this.createZoomInfoDisplay();
    }
  },
  
  // Show canvas controls help
  showControlsHelp() {
    const helpTooltip = document.createElement('div');
    helpTooltip.id = 'canvasControlsHelp';
    helpTooltip.style.position = 'absolute';
    helpTooltip.style.top = '10px';
    helpTooltip.style.right = '10px';
    helpTooltip.style.background = 'rgba(0, 0, 0, 0.7)';
    helpTooltip.style.color = 'white';
    helpTooltip.style.padding = '10px 15px';
    helpTooltip.style.borderRadius = '5px';
    helpTooltip.style.fontSize = '14px';
    helpTooltip.style.zIndex = '1000';
    helpTooltip.style.maxWidth = '300px';
    helpTooltip.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
    
    helpTooltip.innerHTML = `
      <div style="font-weight: bold; margin-bottom: 8px;">画布控制:</div>
      <ul style="margin: 0; padding-left: 20px;">
        <li>鼠标滚轮: 缩放画布</li>
        <li>按住空格键+拖拽: 移动画布</li>
        <li>中键拖拽: 移动画布</li>
        <li>按R键: 重置缩放和位置</li>
      </ul>
      <div style="text-align: right; margin-top: 8px; cursor: pointer; font-size: 12px;" id="closeHelp">关闭</div>
    `;
    
    const container = document.querySelector('.workflow-editor-container');
    if (container) {
      container.appendChild(helpTooltip);
    } else {
      document.getElementById('workflowCanvas').parentNode.appendChild(helpTooltip);
    }
    
    // Add close button functionality
    document.getElementById('closeHelp').addEventListener('click', () => {
      helpTooltip.style.display = 'none';
    });
    
    // Auto-hide after 10 seconds
    setTimeout(() => {
      if (helpTooltip.parentNode) {
        helpTooltip.style.opacity = '0';
        helpTooltip.style.transition = 'opacity 1s ease-out';
        setTimeout(() => {
          if (helpTooltip.parentNode) {
            helpTooltip.parentNode.removeChild(helpTooltip);
          }
        }, 1000);
      }
    }, 10000);
  }
};

// Export the CanvasManager object
window.CanvasManager = CanvasManager; 
 /**
 * Workflow State Management
 * Manages the global state for the workflow editor
 */

// Workflow data state
const WorkflowState = {
  // Current workflow data
  currentWorkflow: {
    id: null,
    name: '',
    description: '',
    nodes: [],
    startNodeId: null,
    globalVariables: {},
    isActive: true
  },
  
  // Node editing state
  nodeIdCounter: 1,
  currentEditingNode: null,
  
  // Initialize workflow state
  init() {
    this.resetWorkflow();
  },
  
  // Reset workflow to default state
  resetWorkflow() {
    this.currentWorkflow = {
      id: null,
      name: '',
      description: '',
      nodes: [],
      startNodeId: null,
      globalVariables: {},
      isActive: true
    };
    this.nodeIdCounter = 1;
    this.currentEditingNode = null;
  },
  
  // Set current workflow data
  setWorkflow(workflow) {
    this.currentWorkflow = workflow;
    
    // Find the highest node ID to set the counter
    let maxId = 0;
    this.currentWorkflow.nodes.forEach(node => {
      const idMatch = node.id.match(/node_(\d+)/);
      if (idMatch) {
        const id = parseInt(idMatch[1]);
        if (id > maxId) {
          maxId = id;
        }
      }
    });
    this.nodeIdCounter = maxId + 1;
  },
  
  // Add a new node to the workflow
  addNode(node) {
    this.currentWorkflow.nodes.push(node);
    if (node.type === 'START') {
      this.currentWorkflow.startNodeId = node.id;
    }
  },
  
  // Get a node by ID
  getNodeById(nodeId) {
    return this.currentWorkflow.nodes.find(n => n.id === nodeId);
  },
  
  // Update a node's properties
  updateNode(nodeId, properties) {
    const node = this.getNodeById(nodeId);
    if (node) {
      Object.assign(node, properties);
    }
  },
  
  // Remove a node from the workflow
  removeNode(nodeId) {
    const index = this.currentWorkflow.nodes.findIndex(n => n.id === nodeId);
    if (index !== -1) {
      this.currentWorkflow.nodes.splice(index, 1);
      
      // If removing the start node, clear the startNodeId
      if (this.currentWorkflow.startNodeId === nodeId) {
        this.currentWorkflow.startNodeId = null;
      }
      
      // Remove any connections to this node
      this.currentWorkflow.nodes.forEach(node => {
        if (node.nextNodes) {
          Object.entries(node.nextNodes).forEach(([condition, targetId]) => {
            if (targetId === nodeId) {
              delete node.nextNodes[condition];
            }
          });
        }
      });
    }
  },
  
  // Generate a new unique node ID
  generateNodeId() {
    return `node_${this.nodeIdCounter++}`;
  },
  
  // Set the current editing node
  setEditingNode(node) {
    this.currentEditingNode = node;
  },
  
  // Clear the current editing node
  clearEditingNode() {
    this.currentEditingNode = null;
  }
};

// Export the WorkflowState object
window.WorkflowState = WorkflowState;
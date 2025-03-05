/**
 * Workflow History Manager
 * Handles version history tracking for the workflow editor
 */

const WorkflowHistory = {
  // Maximum number of history versions to keep
  MAX_HISTORY_VERSIONS: 5,

  // History state
  workflowHistory: [],
  lastSavedVersion: null,
  historyCheckInterval: null,

  /**
   * Initializes the workflow history manager
   * @param {Object} initialWorkflow - The initial workflow state
   */
  initWorkflowHistory(initialWorkflow) {
    this.workflowHistory = [];
    this.lastSavedVersion = JSON.stringify(initialWorkflow);
    
    // Add initial version to history
    this.addToHistory(initialWorkflow);
    
    // Start periodic history check
    this.startHistoryCheck();
  },

  /**
   * Starts the periodic history check
   */
  startHistoryCheck() {
    // Clear any existing interval
    if (this.historyCheckInterval) {
      clearInterval(this.historyCheckInterval);
    }
    
    // Check for changes every 5 seconds
    this.historyCheckInterval = setInterval(() => {
      this.checkForChanges();
    }, 5000);
  },

  /**
   * Stops the periodic history check
   */
  stopHistoryCheck() {
    if (this.historyCheckInterval) {
      clearInterval(this.historyCheckInterval);
      this.historyCheckInterval = null;
    }
  },

  /**
   * Checks for changes in the current workflow compared to the last saved version
   */
  checkForChanges() {
    const currentWorkflowStr = JSON.stringify(WorkflowState.currentWorkflow);
    
    // If there are changes, add to history
    if (currentWorkflowStr !== this.lastSavedVersion) {
      this.addToHistory(JSON.parse(JSON.stringify(WorkflowState.currentWorkflow)));
      this.lastSavedVersion = currentWorkflowStr;
    }
  },

  /**
   * Adds a workflow version to the history
   * @param {Object} workflow - The workflow to add to history
   */
  addToHistory(workflow) {
    // Create a timestamped version
    const version = {
      timestamp: new Date().toISOString(),
      workflow: JSON.parse(JSON.stringify(workflow))
    };
    
    // Add to history
    this.workflowHistory.unshift(version);
    
    // Limit history size
    if (this.workflowHistory.length > this.MAX_HISTORY_VERSIONS) {
      this.workflowHistory = this.workflowHistory.slice(0, this.MAX_HISTORY_VERSIONS);
    }
    
    // Log history update (for debugging)
    console.log(`Workflow history updated. ${this.workflowHistory.length} versions saved.`);
  },

  /**
   * Gets the workflow history
   * @returns {Array} - The workflow history
   */
  getWorkflowHistory() {
    return this.workflowHistory;
  },

  /**
   * Restores a workflow from history
   * @param {number} index - The index of the version to restore
   * @returns {Object} - The restored workflow
   */
  restoreFromHistory(index) {
    if (index >= 0 && index < this.workflowHistory.length) {
      const restoredWorkflow = JSON.parse(JSON.stringify(this.workflowHistory[index].workflow));
      this.lastSavedVersion = JSON.stringify(restoredWorkflow);
      return restoredWorkflow;
    }
    return null;
  },

  /**
   * Forces an update to the history
   * @param {Object} workflow - The workflow to add to history
   */
  forceHistoryUpdate(workflow) {
    const workflowStr = JSON.stringify(workflow);
    if (workflowStr !== this.lastSavedVersion) {
      this.addToHistory(workflow);
      this.lastSavedVersion = workflowStr;
    }
  }
};

// Export the WorkflowHistory object
window.WorkflowHistory = WorkflowHistory;

// For backward compatibility
window.initWorkflowHistory = WorkflowHistory.initWorkflowHistory.bind(WorkflowHistory);
window.forceHistoryUpdate = WorkflowHistory.forceHistoryUpdate.bind(WorkflowHistory); 
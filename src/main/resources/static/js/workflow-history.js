/**
 * Workflow History Manager
 * Handles version history tracking for the workflow editor
 */

// Maximum number of history versions to keep
const MAX_HISTORY_VERSIONS = 5;

// History state
let workflowHistory = [];
let lastSavedVersion = null;
let historyCheckInterval = null;

/**
 * Initializes the workflow history manager
 * @param {Object} initialWorkflow - The initial workflow state
 */
function initWorkflowHistory(initialWorkflow) {
  workflowHistory = [];
  lastSavedVersion = JSON.stringify(initialWorkflow);
  
  // Add initial version to history
  addToHistory(initialWorkflow);
  
  // Start periodic history check
  startHistoryCheck();
}

/**
 * Starts the periodic history check
 */
function startHistoryCheck() {
  // Clear any existing interval
  if (historyCheckInterval) {
    clearInterval(historyCheckInterval);
  }
  
  // Check for changes every 5 seconds
  historyCheckInterval = setInterval(() => {
    checkForChanges();
  }, 5000);
}

/**
 * Stops the periodic history check
 */
function stopHistoryCheck() {
  if (historyCheckInterval) {
    clearInterval(historyCheckInterval);
    historyCheckInterval = null;
  }
}

/**
 * Checks for changes in the current workflow compared to the last saved version
 */
function checkForChanges() {
  const currentWorkflowStr = JSON.stringify(currentWorkflow);
  
  // If there are changes, add to history
  if (currentWorkflowStr !== lastSavedVersion) {
    addToHistory(JSON.parse(JSON.stringify(currentWorkflow)));
    lastSavedVersion = currentWorkflowStr;
  }
}

/**
 * Adds a workflow version to the history
 * @param {Object} workflow - The workflow to add to history
 */
function addToHistory(workflow) {
  // Create a timestamped version
  const version = {
    timestamp: new Date().toISOString(),
    workflow: JSON.parse(JSON.stringify(workflow))
  };
  
  // Add to history
  workflowHistory.unshift(version);
  
  // Limit history size
  if (workflowHistory.length > MAX_HISTORY_VERSIONS) {
    workflowHistory = workflowHistory.slice(0, MAX_HISTORY_VERSIONS);
  }
  
  // Log history update (for debugging)
  console.log(`Workflow history updated. ${workflowHistory.length} versions saved.`);
}

/**
 * Gets the workflow history
 * @returns {Array} - The workflow history
 */
function getWorkflowHistory() {
  return workflowHistory;
}

/**
 * Restores a workflow from history
 * @param {number} index - The index of the version to restore
 * @returns {Object} - The restored workflow
 */
function restoreFromHistory(index) {
  if (index >= 0 && index < workflowHistory.length) {
    const restoredWorkflow = JSON.parse(JSON.stringify(workflowHistory[index].workflow));
    lastSavedVersion = JSON.stringify(restoredWorkflow);
    return restoredWorkflow;
  }
  return null;
}

/**
 * Forces an update to the history
 * @param {Object} workflow - The workflow to add to history
 */
function forceHistoryUpdate(workflow) {
  const workflowStr = JSON.stringify(workflow);
  if (workflowStr !== lastSavedVersion) {
    addToHistory(workflow);
    lastSavedVersion = workflowStr;
  }
} 
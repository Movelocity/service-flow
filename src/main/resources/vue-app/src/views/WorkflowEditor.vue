<script setup lang="ts">
/**
 * WorkflowEditor component
 * Main view for editing a workflow
 */
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useWorkflowStore } from '../stores/workflowStore'
import WorkflowCanvas from '../components/WorkflowCanvas.vue'
import WorkflowToolbar from '../components/WorkflowToolbar.vue'
import NodePropertiesPanel from '../components/NodePropertiesPanel.vue'
import WorkflowExecutionHistory from '../components/WorkflowExecutionHistory.vue'
import WorkflowTestPanel from '../components/WorkflowTestPanel.vue'

const router = useRouter()
const route = useRoute()
const workflowStore = useWorkflowStore()

// Props
const id = computed(() => route.params.id as string)
const isNew = computed(() => route.query.new === 'true')

// Workflow data
const workflow = ref<any>(null)
const isLoading = ref(true)
const error = ref<string | null>(null)
const selectedNodeId = ref<string | null>(null)
const selectedConnectionId = ref<string | null>(null)
const showPropertiesPanel = ref(false)

// Test workflow
const showTestPanel = ref(false)

// Load workflow data
onMounted(async () => {
  if (isNew.value) {
    // Create a new workflow
    workflow.value = workflowStore.createNewWorkflow()
    isLoading.value = false
  } else {
    // Load existing workflow
    try {
      await workflowStore.fetchWorkflow(id.value)
      workflow.value = workflowStore.currentWorkflow
    } catch (err: any) {
      error.value = err.message || 'Failed to load workflow'
      console.error('Error loading workflow:', err)
    } finally {
      isLoading.value = false
    }
  }
})

// Watch for changes in the current workflow
watch(() => workflowStore.currentWorkflow, (newWorkflow) => {
  workflow.value = newWorkflow
})

// Derive connections from nodes' nextNodes relationships
const workflowConnections = computed(() => {
  if (!workflow.value || !workflow.value.nodes) return []
  
  const connections: any[] = []
  let connectionId = 1
  
  workflow.value.nodes.forEach((node: any) => {
    if (node.nextNodes) {
      Object.entries(node.nextNodes).forEach(([key, targetNodeId]: [string, any]) => {
        if (targetNodeId) {
          connections.push({
            id: `connection_${connectionId++}`,
            sourceId: node.id,
            targetId: targetNodeId,
            label: key !== 'default' ? key : '',
            type: 'bezier'
          })
        }
      })
    }
  })
  
  return connections
})

// Handle updates to connections by updating nodes' nextNodes
const handleConnectionsUpdated = (newConnections: any[]) => {
  if (!workflow.value || !workflow.value.nodes) return
  
  // Reset all nextNodes
  workflow.value.nodes.forEach((node: any) => {
    node.nextNodes = {}
  })
  
  // Rebuild nextNodes from connections
  newConnections.forEach((connection: any) => {
    const sourceNode = workflow.value.nodes.find((node: any) => node.id === connection.sourceId)
    if (sourceNode) {
      const key = connection.label || 'default'
      sourceNode.nextNodes[key] = connection.targetId
    }
  })
}

// Save the workflow
const saveWorkflow = async () => {
  if (!workflow.value) return
  
  try {
    await workflowStore.saveWorkflow(workflow.value)
    alert('Workflow saved successfully')
  } catch (err: any) {
    error.value = err.message || 'Failed to save workflow'
    console.error('Error saving workflow:', err)
    alert('Failed to save workflow')
  }
}

// Go back to the workflow list
const goBack = () => {
  router.push({ name: 'Home' })
}

// Get the selected node
const selectedNode = computed(() => {
  if (!selectedNodeId.value || !workflow.value) return null
  return workflow.value.nodes.find((node: any) => node.id === selectedNodeId.value) || null
})

// Handle node selection
const handleNodeSelected = (nodeId: string | null) => {
  selectedNodeId.value = nodeId
  showPropertiesPanel.value = !!nodeId
}

// Handle connection selection
const handleConnectionSelected = (connectionId: string | null) => {
  selectedConnectionId.value = connectionId
  // Could show a connection properties panel here
}

// Handle node updates
const handleNodeUpdated = (updatedNode: any) => {
  if (!workflow.value) return
  
  const nodeIndex = workflow.value.nodes.findIndex((node: any) => node.id === updatedNode.id)
  if (nodeIndex !== -1) {
    workflow.value.nodes[nodeIndex] = updatedNode
  }
  
  showPropertiesPanel.value = false
}

// Handle adding a new node
const handleNodeAdded = (node: any) => {
  if (!workflow.value) return
  workflow.value.nodes.push(node)
}

// Handle toolbar actions
const handleAddNode = (type: string) => {
  if (!workflow.value) return
  
  // Create a new node at a random position
  const newNode = {
    id: 'node_' + Date.now(),
    type,
    name: `New ${type.charAt(0).toUpperCase() + type.slice(1)} Node`,
    position: {
      x: Math.random() * 500,
      y: Math.random() * 300
    },
    config: {}
  }
  
  workflow.value.nodes.push(newNode)
}

const handleClearWorkflow = () => {
  if (!workflow.value) return
  
  workflow.value.nodes = []
  workflow.value.connections = []
}

const handleAutoLayout = () => {
  // This would implement an auto-layout algorithm
  alert('Auto-layout functionality will be implemented')
}

const handleZoomIn = () => {
  // This would be handled by the canvas component
}

const handleZoomOut = () => {
  // This would be handled by the canvas component
}

const handleZoomReset = () => {
  // This would be handled by the canvas component
}

const handleTestWorkflow = async () => {
  if (!workflow.value) return
  showTestPanel.value = true
}

const handleTestComplete = (executionId: string) => {
  // Refresh the execution history
  // In a real implementation, we would refresh the execution history
}

// Close the properties panel
const closePropertiesPanel = () => {
  showPropertiesPanel.value = false
  selectedNodeId.value = null
}
</script>

<template>
  <div class="header">
    <div class="container-fluid">
      <div class="d-flex justify-content-between align-items-center">
        <h1>{{ isNew ? '创建新工作流' : '编辑工作流' }}</h1>
        <div>
          <button @click="goBack" class="btn btn-outline-secondary me-2">返回</button>
          <button @click="saveWorkflow" class="btn btn-primary" :disabled="isLoading">保存</button>
        </div>
      </div>
    </div>
  </div>

  <div class="container-fluid">
    <div v-if="isLoading" class="text-center my-5">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="error" class="alert alert-danger" role="alert">
      {{ error }}
    </div>

    <div v-else-if="workflow" class="workflow-editor">
      <div class="row mb-4">
        <div class="col-md-6">
          <div class="mb-3">
            <label for="workflowName" class="form-label">工作流名称</label>
            <input 
              type="text" 
              class="form-control" 
              id="workflowName" 
              v-model="workflow.name" 
              placeholder="输入工作流名称"
            >
          </div>
        </div>
        <div class="col-md-6">
          <div class="mb-3">
            <label for="workflowStatus" class="form-label">状态</label>
            <select class="form-select" id="workflowStatus" v-model="workflow.isActive">
              <option :value="true">激活</option>
              <option :value="false">未激活</option>
            </select>
          </div>
        </div>
      </div>

      <div class="mb-4">
        <label for="workflowDescription" class="form-label">描述</label>
        <textarea 
          class="form-control" 
          id="workflowDescription" 
          v-model="workflow.description" 
          rows="3" 
          placeholder="输入工作流描述"
        ></textarea>
      </div>

      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">工作流设计器</h5>
        </div>
        <div class="card-body p-0">
          <!-- Workflow Toolbar -->
          <WorkflowToolbar
            @add-node="handleAddNode"
            @clear="handleClearWorkflow"
            @auto-layout="handleAutoLayout"
            @zoom-in="handleZoomIn"
            @zoom-out="handleZoomOut"
            @zoom-reset="handleZoomReset"
            @test-workflow="handleTestWorkflow"
          />
          
          <!-- Workflow Canvas -->
          <div class="workflow-canvas-container">
            <WorkflowCanvas
              v-model:nodes="workflow.nodes"
              :connections="workflowConnections"
              @update:connections="handleConnectionsUpdated"
              @node-selected="handleNodeSelected"
              @connection-selected="handleConnectionSelected"
              @node-added="handleNodeAdded"
            />
            
            <!-- Node Properties Panel -->
            <NodePropertiesPanel
              v-if="showPropertiesPanel"
              :node="selectedNode"
              @update:node="handleNodeUpdated"
              @close="closePropertiesPanel"
            />
          </div>
        </div>
      </div>

      <!-- Workflow Execution History -->
      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">工作流执行历史</h5>
        </div>
        <div class="card-body">
          <WorkflowExecutionHistory :workflow-id="id" />
        </div>
      </div>

      <!-- Workflow Test Panel -->
      <WorkflowTestPanel 
        :workflow-id="id" 
        :show="showTestPanel" 
        @close="showTestPanel = false"
        @test-complete="handleTestComplete"
      />
      
      <!-- Modal backdrop for test panel -->
      <div 
        class="modal-backdrop fade show" 
        v-if="showTestPanel"
        @click="showTestPanel = false"
      ></div>
    </div>
  </div>
</template>

<style scoped>
.header {
  background-color: #f8f9fa;
  padding: 20px 0;
  margin-bottom: 30px;
  border-bottom: 1px solid #e9ecef;
}

.workflow-editor {
  margin-bottom: 50px;
}

.workflow-canvas-container {
  position: relative;
  height: 600px;
  border: 1px solid #dee2e6;
  border-top: none;
}
</style> 
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '../stores/workflowStore'

const router = useRouter()
const workflowStore = useWorkflowStore()

// Load workflows when the component is mounted
onMounted(async () => {
  await workflowStore.fetchWorkflows()
})

// Create a new workflow
const createNewWorkflow = () => {
  const newWorkflow = workflowStore.createNewWorkflow()
  router.push({
    name: 'WorkflowEditor',
    params: { id: newWorkflow.id },
    query: { new: 'true' }
  })
}

// Open the workflow editor
const openWorkflowEditor = (workflowId: string) => {
  router.push({
    name: 'WorkflowEditor',
    params: { id: workflowId }
  })
}

// Test a workflow
const testWorkflow = async (workflowId: string, event: Event) => {
  event.stopPropagation()
  try {
    // Implement workflow testing logic
    alert('Workflow test functionality will be implemented')
  } catch (error) {
    console.error('Error testing workflow:', error)
    alert('Failed to test workflow')
  }
}

// Delete a workflow
const deleteWorkflow = async (workflowId: string, event: Event) => {
  event.stopPropagation()
  if (!confirm('Are you sure you want to delete this workflow?')) {
    return
  }

  try {
    await workflowStore.deleteWorkflow(workflowId)
    alert('Workflow deleted successfully')
  } catch (error) {
    console.error('Error deleting workflow:', error)
    alert('Failed to delete workflow')
  }
}
</script>

<template>
  <div class="header">
    <div class="container">
      <div class="d-flex justify-content-between align-items-center">
        <h1>工作流管理</h1>
        <button @click="createNewWorkflow" class="btn btn-primary">创建新工作流</button>
      </div>
    </div>
  </div>

  <div class="container">
    <div v-if="workflowStore.loading" class="text-center my-5">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div v-else-if="workflowStore.error" class="alert alert-danger" role="alert">
      {{ workflowStore.error }}
    </div>

    <div v-else class="row">
      <div v-if="workflowStore.workflows.length === 0" class="col-12 text-center">
        <p class="text-muted">No workflows found. Create a new one to get started.</p>
      </div>

      <div v-for="workflow in workflowStore.workflows" :key="workflow.id" class="col-md-4 mb-4">
        <div class="card workflow-card h-100" @click="openWorkflowEditor(workflow.id)">
          <div class="card-body">
            <h5 class="card-title">{{ workflow.name || 'Unnamed Workflow' }}</h5>
            <p class="card-text">{{ workflow.description || 'No description' }}</p>
            <span class="card-subtitle mb-2 text-muted">ID: {{ workflow.id }}</span>
            <div class="d-flex justify-content-between">
              <small class="text-muted">Status: {{ workflow.isActive ? 'Active' : 'Inactive' }}</small>
              <small class="text-muted">Nodes: {{ workflow.nodes.length }}</small>
            </div>
          </div>
          <div class="card-footer bg-transparent d-flex justify-content-end gap-2">
            <button class="btn btn-sm btn-outline-primary" @click.stop="openWorkflowEditor(workflow.id)">编辑</button>
            <button class="btn btn-sm btn-outline-success" @click="testWorkflow(workflow.id, $event)">测试</button>
            <button class="btn btn-sm btn-outline-danger" @click="deleteWorkflow(workflow.id, $event)">删除</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.workflow-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.workflow-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
}

.header {
  background-color: #f8f9fa;
  padding: 20px 0;
  margin-bottom: 30px;
  border-bottom: 1px solid #e9ecef;
}
</style> 
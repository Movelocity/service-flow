<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowStore } from '../stores/workflowStore'

// Props
const props = defineProps<{
  id: string
  isNew?: boolean
}>()

const router = useRouter()
const workflowStore = useWorkflowStore()

// Workflow data
const workflow = ref(null)
const isLoading = ref(true)
const error = ref(null)

// Load workflow data
onMounted(async () => {
  if (props.isNew) {
    // Create a new workflow
    workflow.value = workflowStore.createNewWorkflow()
    isLoading.value = false
  } else {
    // Load existing workflow
    try {
      await workflowStore.fetchWorkflow(props.id)
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
</script>

<template>
  <div class="header">
    <div class="container">
      <div class="d-flex justify-content-between align-items-center">
        <h1>{{ props.isNew ? '创建新工作流' : '编辑工作流' }}</h1>
        <div>
          <button @click="goBack" class="btn btn-outline-secondary me-2">返回</button>
          <button @click="saveWorkflow" class="btn btn-primary" :disabled="isLoading">保存</button>
        </div>
      </div>
    </div>
  </div>

  <div class="container">
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
        <div class="card-body">
          <p class="text-muted">工作流设计器将在这里实现</p>
          <div class="workflow-canvas" style="height: 400px; border: 1px dashed #ccc; border-radius: 4px;">
            <!-- Workflow canvas will be implemented here -->
          </div>
        </div>
      </div>
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
</style> 
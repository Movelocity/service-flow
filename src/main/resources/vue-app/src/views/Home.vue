<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import workflowApi from '../api/workflow'

const workflows = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const router = useRouter()

// Fetch workflows on component mount
onMounted(async () => {
  try {
    loading.value = true
    // 首先获取工作流ID列表
    const workflowIds = await workflowApi.fetchWorkflows()
    console.log('获取到的工作流ID列表:', workflowIds)
    
    // 然后获取每个工作流的详细信息
    const workflowPromises = workflowIds.map(id => workflowApi.fetchWorkflow(id))
    const workflowDetails = await Promise.all(workflowPromises)
    console.log('获取到的工作流详细信息:', workflowDetails)
    
    workflows.value = workflowDetails
  } catch (err) {
    error.value = '加载工作流失败，请重试。'
    console.error(err)
  } finally {
    loading.value = false
  }
})

// Navigate to workflow editor
const editWorkflow = (workflowId: string) => {
  console.log('编辑工作流ID:', workflowId)
  if (!workflowId) {
    console.error('工作流ID为空')
    return
  }
  router.push(`/workflow/editor/${workflowId}`)
}

// Create a new workflow
const createWorkflow = () => {
  router.push('/workflow/editor/new')
}

// Delete a workflow
const deleteWorkflow = async (workflowId: string) => {
  if (!workflowId) {
    console.error('工作流ID为空')
    return
  }
  
  if (!confirm('确定要删除这个工作流吗？')) return
  
  try {
    await workflowApi.deleteWorkflow(workflowId)
    // Refresh the list after deletion
    workflows.value = workflows.value.filter(w => w.id !== workflowId)
  } catch (err) {
    alert('删除工作流失败')
    console.error(err)
  }
}
</script>

<template>
  <div class="container">
    <div class="header">
      <h1>工作流管理</h1>
      <button class="button primary" @click="createWorkflow">
        <span class="icon-plus"></span>创建新工作流
      </button>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-else-if="workflows.length === 0" class="info-message">
      没有找到工作流。创建您的第一个工作流开始使用。
    </div>

    <div v-else>
      <div class="debug-info" style="margin-bottom: 16px; padding: 8px; background-color: #f0f0f0; border-radius: 4px;">
        <details>
          <summary>调试信息</summary>
          <pre>{{ JSON.stringify(workflows, null, 2) }}</pre>
        </details>
      </div>
      
      <div class="card-grid">
        <div v-for="workflow in workflows" :key="workflow.id" class="card">
          <div class="card-content">
            <h2 class="card-title">{{ workflow.name || '未命名工作流' }}</h2>
            <p class="card-description">{{ workflow.description || '无描述' }}</p>
            <p class="card-status">
              状态: <span :class="workflow.active ? 'status-active' : 'status-inactive'">
                {{ workflow.active ? '激活' : '未激活' }}
              </span>
            </p>
            <p class="card-id">ID: {{ workflow.id }}</p>
            <div class="card-actions">
              <button class="button outline" @click="editWorkflow(workflow.id)">
                <span class="icon-edit"></span>编辑
              </button>
              <button class="button danger outline" @click="deleteWorkflow(workflow.id)">
                <span class="icon-delete"></span>删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  border: 1px solid transparent;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.button.primary {
  background-color: #4f46e5;
  color: white;
}

.button.primary:hover {
  background-color: #4338ca;
}

.button.outline {
  background-color: transparent;
  border: 1px solid #d1d5db;
  color: #374151;
}

.button.outline:hover {
  background-color: #f9fafb;
}

.button.danger {
  color: #ef4444;
  border-color: #ef4444;
}

.button.danger:hover {
  background-color: #fee2e2;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #4f46e5;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-message {
  padding: 16px;
  background-color: #fee2e2;
  color: #b91c1c;
  border-radius: 4px;
  margin-bottom: 16px;
}

.info-message {
  padding: 16px;
  background-color: #e0f2fe;
  color: #0369a1;
  border-radius: 4px;
  margin-bottom: 16px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-content {
  padding: 16px;
}

.card-title {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 18px;
}

.card-description {
  color: #6b7280;
  margin-bottom: 12px;
}

.card-status {
  font-size: 14px;
  margin-bottom: 16px;
}

.status-active {
  color: #10b981;
}

.status-inactive {
  color: #ef4444;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.icon-plus::before {
  content: "+";
  font-weight: bold;
}

.icon-edit::before {
  content: "✎";
}

.icon-delete::before {
  content: "×";
}

.card-id {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 12px;
  font-family: monospace;
}
</style> 
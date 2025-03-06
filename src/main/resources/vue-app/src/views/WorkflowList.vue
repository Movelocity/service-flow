<template>
  <div class="workflow-list">
    <div class="list-header">
      <h1>工作流列表</h1>
      <button class="btn btn-primary" @click="createWorkflow">
        新建工作流
      </button>
    </div>

    <div class="list-content">
      <div v-if="isLoading" class="loading">
        加载中...
      </div>
      
      <div v-else-if="workflows.length === 0" class="empty-state">
        暂无工作流，点击"新建工作流"开始创建
      </div>
      
      <div v-else class="workflow-grid">
        <div
          v-for="workflow in workflows"
          :key="workflow.id"
          class="workflow-card"
          @click="openWorkflow(workflow.id)"
        >
          <div class="card-header">
            <h3 class="workflow-name">{{ workflow.name }}</h3>
            <div class="workflow-actions">
              <button
                class="btn btn-sm btn-outline-danger"
                @click.stop="deleteWorkflow(workflow.id)"
              >
                删除
              </button>
            </div>
          </div>
          
          <div class="workflow-description">
            {{ workflow.description || '暂无描述' }}
          </div>
          
          <div class="workflow-meta">
            <span class="nodes-count">
              {{ workflow.nodes?.length || 0 }} 个节点
            </span>
            <span class="update-time">
              最后更新: {{ formatDate(workflow.updatedAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { Workflow } from '../types/workflow';
import { workflowApi } from '../services/workflowApi';

const router = useRouter();
const isLoading = ref(true);
const workflows = ref<Workflow[]>([]);

// 加载工作流列表
async function loadWorkflows() {
  try {
    workflows.value = await workflowApi.listWorkflows();
  } catch (error) {
    console.error('Failed to load workflows:', error);
  } finally {
    isLoading.value = false;
  }
}

// 创建新工作流
function createWorkflow() {
  router.push({
    name: 'workflow-editor',
    params: { id: 'new' }
  });
}

// 打开工作流
function openWorkflow(id: string) {
  router.push({
    name: 'workflow-editor',
    params: { id }
  });
}

// 删除工作流
async function deleteWorkflow(id: string) {
  if (!confirm('确定要删除这个工作流吗？')) return;
  
  try {
    await workflowApi.deleteWorkflow(id);
    workflows.value = workflows.value.filter(w => w.id !== id);
  } catch (error) {
    console.error('Failed to delete workflow:', error);
    alert('删除工作流失败');
  }
}

// 格式化日期
function formatDate(date: Date | undefined): string {
  if (!date) return '未知';
  return new Date(date).toLocaleString();
}

// 组件挂载时加载数据
onMounted(() => {
  loadWorkflows();
});
</script>

<style scoped>
.workflow-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.loading,
.empty-state {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.workflow-card {
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.workflow-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.workflow-name {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 500;
}

.workflow-description {
  color: #6c757d;
  margin-bottom: 16px;
  min-height: 40px;
}

.workflow-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #6c757d;
}

.nodes-count {
  background: #f8f9fa;
  padding: 2px 8px;
  border-radius: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .workflow-list {
    padding: 10px;
  }
  
  .workflow-grid {
    grid-template-columns: 1fr;
  }
}
</style> 
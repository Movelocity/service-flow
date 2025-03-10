<template>
  <div class="workflow-list">
    <div class="list-header">
      <div class="header-left">
        <h1>工作流列表</h1>
      </div>
      <div class="header-right">
        <ThemeButton />
        <button class="btn btn-primary" @click="createWorkflow">
          新建工作流
        </button>
      </div>
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
import type { Workflow } from '@/types/workflow';
import { workflowApi } from '@/services/workflowApi';
import ThemeButton from '@/components/ThemeButton.vue';

const router = useRouter();
const isLoading = ref(true);
const workflows = ref<Workflow[]>([]);

// 加载工作流列表
onMounted(async () => {
  try {
    workflows.value = await workflowApi.listWorkflows();
  } catch (error) {
    console.error('Failed to load workflows:', error);
  } finally {
    isLoading.value = false;
  }
});

// 创建新工作流
async function createWorkflow() {
  try {
    const newWorkflow: Partial<Workflow> = {
      name: '新工作流',
      description: '',
      inputs: {},
      outputs: {},
      tools: {},
      globalVariables: {},
      nodes: [],
      startNodeId: '',
      isActive: true
    };
    
    const createdWorkflow = await workflowApi.saveWorkflow(newWorkflow);
    router.push({
      name: 'workflow-editor',
      params: { id: createdWorkflow.id }
    });
  } catch (error) {
    console.error('Failed to create workflow:', error);
    alert('创建工作流失败');
  }
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
</script>

<style scoped>
.workflow-list {
  padding: 20px;
  min-height: 100vh;
  background-color: var(--background-color);
  color: var(--text-color);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.workflow-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.workflow-card {
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: var(--card-shadow);
}

.workflow-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.workflow-name {
  margin: 0;
  font-size: 1.2em;
  color: var(--text-color);
}

.workflow-description {
  color: var(--text-color);
  opacity: 0.8;
  margin-bottom: 15px;
}

.workflow-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.9em;
  color: var(--text-color);
  opacity: 0.6;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: var(--text-color);
  opacity: 0.6;
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
<template>
  <div class="workflow-editor">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <button class="btn btn-link" @click="navigateBack">
          <span class="back-icon">←</span> 返回
        </button>
        <div class="workflow-info">
          <input
            v-model="workflowName"
            type="text"
            class="form-control form-control-sm"
            placeholder="工作流名称"
            @change="updateWorkflowInfo"
          >
          <input
            v-model="workflowDescription"
            type="text"
            class="form-control form-control-sm ms-2"
            placeholder="描述"
            @change="updateWorkflowInfo"
          >
        </div>
      </div>
      <div class="toolbar-right">
        <span v-if="executionStatus" class="execution-status me-2">
          执行状态: {{ executionStatus }}
        </span>
        <button
          class="btn btn-primary btn-sm me-2"
          @click="saveWorkflow"
          :disabled="!isDirty"
        >
          保存
        </button>
        <button
          class="btn btn-secondary btn-sm me-2"
          @click="testWorkflow"
          :disabled="!canTest || executionStatus === '执行中...'"
        >
          测试
        </button>
        <button
          class="btn btn-outline-secondary btn-sm me-2"
          @click="undo"
          :disabled="!canUndo"
        >
          撤销
        </button>
        <button
          class="btn btn-outline-secondary btn-sm"
          @click="redo"
          :disabled="!canRedo"
        >
          重做
        </button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-main">
      <WorkflowCanvas />
      <NodeEditor :is-visible="isEditorPanelOpen" @close="closeEditor" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWorkflowStore } from '../stores/workflow';
import { workflowApi } from '../services/workflowApi';
import WorkflowCanvas from '../components/WorkflowCanvas.vue';
import NodeEditor from '../components/NodeEditor.vue';

const router = useRouter();
const route = useRoute();
const store = useWorkflowStore();

// 工作流信息
const workflowName = ref('');
const workflowDescription = ref('');
const isDirty = ref(false);
const isLoading = ref(true);
const executionStatus = ref<string | null>(null);

// 编辑器状态
const isEditorPanelOpen = computed(() => store.editorState.isEditorPanelOpen);

// 计算属性
const canUndo = computed(() => store.history.past.length > 0);
const canRedo = computed(() => store.history.future.length > 0);
const canTest = computed(() => {
  const workflow = store.currentWorkflow;
  if (!workflow) return false;
  
  // 检查是否有开始节点和结束节点
  const hasStart = workflow.nodes.some(node => node.type === 'START');
  const hasEnd = workflow.nodes.some(node => node.type === 'END');
  
  return hasStart && hasEnd && workflow.nodes.length >= 2;
});

// 监听工作流数据变化
function onWorkflowChange() {
  const workflow = store.currentWorkflow;
  if (workflow) {
    workflowName.value = workflow.name;
    workflowDescription.value = workflow.description;
  }
}

// 监听工作流变化
watch(() => store.currentWorkflow, (newWorkflow) => {
  if (newWorkflow) {
    // 当工作流发生变化时，设置isDirty为true
    // 但在初始加载和保存后的重新加载时不设置
    if (store.history.past.length > 0) {
      isDirty.value = true;
    }
  }
}, { deep: true });

// 更新工作流信息
function updateWorkflowInfo() {
  if (!store.currentWorkflow) return;
  
  store.currentWorkflow.name = workflowName.value;
  store.currentWorkflow.description = workflowDescription.value;
  isDirty.value = true;
}

// 保存工作流
async function saveWorkflow() {
  if (!store.currentWorkflow) return;
  
  try {
    const savedWorkflow = await workflowApi.saveWorkflow(store.currentWorkflow);
    store.loadWorkflow(savedWorkflow);
    isDirty.value = false;
  } catch (error) {
    console.error('Failed to save workflow:', error);
    alert('保存工作流失败');
  }
}

// 测试工作流
async function testWorkflow() {
  if (!store.currentWorkflow?.id) return;
  
  try {
    executionStatus.value = '执行中...';
    const executionId = await workflowApi.executeWorkflow(store.currentWorkflow.id);
    
    // 轮询执行状态
    const checkStatus = async () => {
      const status = await workflowApi.getExecutionStatus(store.currentWorkflow!.id, executionId);
      executionStatus.value = status;
      
      if (status !== 'COMPLETED' && status !== 'FAILED') {
        setTimeout(checkStatus, 1000);
      }
    };
    
    checkStatus();
  } catch (error) {
    console.error('Failed to test workflow:', error);
    alert('测试工作流失败');
    executionStatus.value = null;
  }
}

// 撤销/重做
function undo() {
  store.undo();
  isDirty.value = true;
}

function redo() {
  store.redo();
  isDirty.value = true;
}

// 关闭编辑器面板
function closeEditor() {
  store.setSelectedNode(null);
}

// 返回上一页
function navigateBack() {
  if (isDirty.value) {
    if (confirm('有未保存的更改，确定要离开吗？')) {
      router.back();
    }
  } else {
    router.back();
  }
}

// 离开页面前确认
onBeforeUnmount(() => {
  if (isDirty.value) {
    const result = confirm('有未保存的更改，确定要离开吗？');
    if (!result) {
      // 阻止离开
      throw new Error('Navigation cancelled');
    }
  }
});

// 加载工作流
async function loadWorkflow(id: string) {
  try {
    isLoading.value = true;
    const workflow = await workflowApi.getWorkflow(id);
    store.loadWorkflow(workflow);
    workflowName.value = workflow.name;
    workflowDescription.value = workflow.description;
  } catch (error) {
    console.error('Failed to load workflow:', error);
    alert('加载工作流失败');
    router.push('/workflows');
  } finally {
    isLoading.value = false;
  }
}

// 组件挂载时初始化
onMounted(async () => {
  const workflowId = route.params.id as string;
  
  if (workflowId === 'new') {
    store.createWorkflow('新工作流', '这是一个新的工作流');
    onWorkflowChange();
  } else {
    await loadWorkflow(workflowId);
  }
});
</script>

<style scoped>
.workflow-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.editor-toolbar {
  height: 60px;
  padding: 0 20px;
  background: white;
  border-bottom: 1px solid #dee2e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-icon {
  font-size: 1.2em;
  margin-right: 4px;
}

.workflow-info {
  display: flex;
  gap: 10px;
}

.workflow-info .form-control {
  width: auto;
}

.editor-main {
  flex: 1;
  position: relative;
  overflow: hidden;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .workflow-info {
    flex-direction: column;
    gap: 5px;
  }
  
  .toolbar-right {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--node-selected);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 1rem;
  color: #6c757d;
}

.execution-status {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: #f8f9fa;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  color: #6c757d;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 
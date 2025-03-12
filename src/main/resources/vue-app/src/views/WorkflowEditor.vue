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
        <ThemeButton />
        <span v-if="executionStatus" class="execution-status me-2">
          执行状态: {{ executionStatus }}
        </span>
        <button
          class="btn btn-primary btn-sm me-2"
          @click="saveWorkflow"
        >
          保存
        </button>
        <button
          class="btn btn-secondary btn-sm me-2"
          @click="testWorkflow"
          :disabled="!canTest || executionStatus === '执行中...'"
        >
          运行
        </button>
        <button
          class="btn btn-info btn-sm"
          @click="debugWorkflow"
          :disabled="!canTest || isDebugging"
        >
          调试
        </button>
      </div>
    </div>

    <!-- 主编辑区域 -->
    <div class="editor-main">
      <WorkflowCanvas />
      <NodeEditor :is-visible="isEditorPanelOpen" @close="closeEditor" />
      <DebugPanel
        v-if="isDebugging"
        :events="debugEvents"
        :workflow-id="workflowId"
        :required-inputs="workflowInputs"
        @stop="stopDebug"
        @debug-start="startDebug"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWorkflowStore } from '@/stores/workflow';
import { workflowApi } from '@/services/workflowApi';
import WorkflowCanvas from '@/components/workflow/WorkflowCanvas.vue';
import NodeEditor from '@/components/NodeEditor.vue';
import ThemeButton from '@/components/ThemeButton.vue';
import DebugPanel from '@/components/DebugPanel.vue';
const router = useRouter();
const route = useRoute();
const store = useWorkflowStore();

// 工作流信息
const workflowName = ref('');
const workflowDescription = ref('');
const isLoading = ref(true);
const executionStatus = ref<string | null>(null);
const isDebugging = ref(false);
const debugEvents = ref<any[]>([]);
const workflowId = computed(() => store.currentWorkflow?.id || '');
const workflowInputs = ref<Record<string, any>>({});

// 调试相关
let eventSource: (() => void) | null = null;
let handleNodeExecution: ((event: CustomEvent) => void) | null = null;
let handleDebugError: ((event: CustomEvent) => void) | null = null;

// 计算属性
const isDirty = computed(() => store.isDirty);
const canTest = computed(() => {
  const workflow = store.currentWorkflow;
  if (!workflow) return false;
  
  // 检查是否有开始节点和结束节点
  const hasStart = workflow.nodes.some(node => node.type === 'START');
  const hasEnd = workflow.nodes.some(node => node.type === 'END');
  
  return hasStart && hasEnd && workflow.nodes.length >= 2;
});

// 编辑器状态
const isEditorPanelOpen = computed(() => store.editorState.isEditorPanelOpen);

// 监听工作流数据变化
function onWorkflowChange() {
  const workflow = store.currentWorkflow;
  if (workflow) {
    workflowName.value = workflow.name;
    workflowDescription.value = workflow.description;
  }
}

// 监听工作流变化
// watch(() => store.currentWorkflow, (newWorkflow, oldWorkflow) => {
//   if (newWorkflow) {
//     // 只有在非初始加载时才设置isDirty
//     if (oldWorkflow && store.history.past.length > 0) {
//       store.saveToHistory();
//     }
//   }
// }, { deep: true });

// 更新工作流信息
function updateWorkflowInfo() {
  if (!store.currentWorkflow) return;
  
  store.currentWorkflow.name = workflowName.value;
  store.currentWorkflow.description = workflowDescription.value;
  // store.saveToHistory();
}

// 保存工作流
async function saveWorkflow() {
  console.log('saveWorkflow');
  try {
    await store.saveWorkflow();
    store.isDirty = false;
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

// 调试工作流
async function debugWorkflow() {
  if (!workflowId.value) return;
  
  try {
    // 获取工作流必填参数
    workflowInputs.value = await workflowApi.getWorkflowInputs(workflowId.value);
    isDebugging.value = true;
    debugEvents.value = [];
  } catch (error) {
    console.error('Failed to get workflow inputs:', error);
    alert('获取工作流参数失败');
  }
}

// 开始调试
async function startDebug(inputs: Record<string, any>) {
  if (!workflowId.value) return;
  
  try {
    // 注册事件监听器
    handleNodeExecution = (event: CustomEvent) => {
      debugEvents.value.push(event.detail);
    };

    handleDebugError = (event: CustomEvent) => {
      console.error('Debug error:', event.detail);
      stopDebug();
      alert('调试过程中发生错误');
    };

    window.addEventListener('node-execution', handleNodeExecution as EventListener);
    window.addEventListener('debug-error', handleDebugError as EventListener);

    // 保存清理函数
    eventSource = workflowApi.debugWorkflow(workflowId.value, inputs);
  } catch (error) {
    console.error('Failed to start workflow debug:', error);
    alert('启动调试失败');
    stopDebug();
  }
}

// 停止调试
function stopDebug() {
  if (eventSource) {
    // 移除事件监听器
    if (handleNodeExecution) {
      window.removeEventListener('node-execution', handleNodeExecution as EventListener);
      handleNodeExecution = null;
    }
    if (handleDebugError) {
      window.removeEventListener('debug-error', handleDebugError as EventListener);
      handleDebugError = null;
    }
    // 调用清理函数关闭连接
    eventSource();
    eventSource = null;
  }
  isDebugging.value = false;
}

// 关闭编辑器面板
function closeEditor() {
  store.selectNode(null);
}

// 返回上一页
function navigateBack() {
  router.back();
}

// 离开页面前确认
onBeforeUnmount(() => {
  // 不需要在这里处理，因为 Vue Router 的导航守卫会处理
});

// 添加全局 beforeunload 事件处理
onMounted(() => {
  const workflowId = route.params.id as string;
  
  // 添加页面刷新或关闭时的提示
  window.addEventListener('beforeunload', (event) => {
    if (isDirty.value) {
      event.preventDefault();
      // 在现代浏览器中，这个消息可能不会显示，而是显示浏览器默认的消息
      event.returnValue = '有未保存的更改，确定要离开吗？';
    }
  });

  if (workflowId === 'new') {
    store.createWorkflow('新工作流', '这是一个新的工作流');
    onWorkflowChange();
  } else {
    loadWorkflow(workflowId);
  }
});

// 组件卸载时移除事件监听器
onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', (event) => {
    if (isDirty.value) {
      event.preventDefault();
      event.returnValue = '有未保存的更改，确定要离开吗？';
    }
  });
  stopDebug();
});

// 加载工作流
async function loadWorkflow(id: string) {
  try {
    isLoading.value = true;
    await store.loadWorkflow(id);
    const workflow = store.currentWorkflow;
    if (workflow) {
      workflowName.value = workflow.name;
      workflowDescription.value = workflow.description;
    }
  } catch (error) {
    console.error('Failed to load workflow:', error);
    alert('加载工作流失败');
    store.isDirty = false;
    router.push('/workflows');
  } finally {
    isLoading.value = false;
  }
}
</script>

<style scoped>
.workflow-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--background-color);
}

.editor-toolbar {
  height: 60px;
  padding: 0 20px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
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
  background-color: var(--node-bg);
  border-color: var(--border-color);
  color: var(--text-color);
}

.workflow-info .form-control:focus {
  background-color: var(--node-bg);
  border-color: var(--node-selected);
  color: var(--text-color);
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.editor-main {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .workflow-info {
    flex-direction: column;
    gap: 5px;
  }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--background-color);
  opacity: 0.9;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--node-selected);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 1rem;
  color: var(--text-color);
}

.execution-status {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
  font-size: 0.875rem;
  color: var(--text-color);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 
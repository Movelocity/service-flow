<template>
  <div class="workflow-editor">
    <!-- 加载状态 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <div class="loading-text">加载中...</div>
    </div>

    <!-- 工具栏 -->
    <!-- <div class="editor-toolbar">
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
          @click="debugWorkflow"
        >
          {{ isDebugging ? '终止调试' : '调试' }}
        </button>
      </div>
    </div> -->
    <EditorToolbar :workflow-name="workflowName" :workflow-description="workflowDescription" />

    <!-- 主编辑区域 -->
    <div class="editor-main">
      <WorkflowCanvas />
      <NodeEditor :is-visible="isEditorPanelOpen" @close="closeEditor" />
      <DebugPanel v-if="isDebugging" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWorkflowStore } from '@/stores/workflow';
import { useDebugStore } from '@/stores/debug';
import WorkflowCanvas from '@/components/workflow/WorkflowCanvas.vue';
import NodeEditor from '@/components/NodeEditor.vue';

import DebugPanel from '@/components/DebugPanel.vue';
import EditorToolbar from '@/components/EditorToolbar.vue';
const router = useRouter();
const route = useRoute();
const store = useWorkflowStore();
const debugStore = useDebugStore();

// 工作流信息
const workflowName = ref('');
const workflowDescription = ref('');
const isLoading = ref(true);

// 计算属性
const isDirty = computed(() => store.isDirty);
const isDebugging = computed(() => debugStore.isDebugging);

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

// 关闭编辑器面板
function closeEditor() {
  store.selectNode(null);
}

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
  // 确保停止调试并清除状态
  debugStore.stopDebug();
  debugStore.resetDebug();
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
      
      // 重置调试状态
      debugStore.resetDebug();
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

.back-icon {
  font-size: 1.2em;
  margin-right: 4px;
}

.editor-main {
  flex: 1;
  position: relative;
  overflow: hidden;
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 
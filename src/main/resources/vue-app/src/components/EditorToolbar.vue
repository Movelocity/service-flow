<template>
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
        >
        <input
          v-model="workflowDescription"
          type="text"
          class="form-control form-control-sm ms-2"
          placeholder="描述"
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
  </div>
</template>

<script setup lang="ts">  

import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useWorkflowStore } from '@/stores/workflow';
import { useDebugStore } from '@/stores/debug';
import ThemeButton from '@/components/common/ThemeButton.vue';

const executionStatus = ref<string | null>(null);
const router = useRouter();
const debugStore = useDebugStore();
const isDebugging = computed(() => debugStore.isDebugging);
const store = useWorkflowStore();

const workflowId = computed(() => store.currentWorkflow?.id || '');

const canTest = computed(() => {
  const workflow = store.currentWorkflow;
  if (!workflow) return false;
  
  // 检查是否有开始节点和结束节点
  const hasStart = workflow.nodes.some(node => node.type === 'START');
  const hasEnd = workflow.nodes.some(node => node.type === 'END');
  
  return hasStart && hasEnd && workflow.nodes.length >= 2;
});


const workflowName = computed({
  get: () => store.currentWorkflow?.name || '',
  set: (value: string) => {
    if (store.currentWorkflow) {
      store.currentWorkflow.name = value;
      store.isDirty = true;
    }
  }
});

const workflowDescription = computed({
  get: () => store.currentWorkflow?.description || '',
  set: (value: string) => {
    if (store.currentWorkflow) {
      store.currentWorkflow.description = value;
      store.isDirty = true;
    }
  }
});

const navigateBack = () => {
  router.back();
};

// 调试工作流
async function debugWorkflow() {
  if (!workflowId.value) return;
  
  console.log('debugWorkflow', isDebugging.value);
  
  if(!isDebugging.value) {
    if(!canTest.value){
      alert('请先添加开始和结束节点');
      return;
    }
    try {
      await debugStore.initDebug(workflowId.value);
    } catch (error) {
      alert(error instanceof Error ? error.message : '获取工作流参数失败');
    }
  } else {
    debugStore.stopDebug();
  }
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

</script>

<style scoped>
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

.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
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

/* 响应式调整 */
@media (max-width: 768px) {
  .workflow-info {
    flex-direction: column;
    gap: 5px;
  }
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

</style>



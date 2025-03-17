<template>
  <div
    v-show="visible"
    class="context-menu"
    :style="{
      left: position.x + 'px',
      top: position.y + 'px'
    }"
  >
    <div
      v-for="type in availableNodeTypes"
      :key="type"
      class="menu-item"
      @click="handleNodeAdd(type)"
    >
      添加{{ nodeTypeLabels[type] }}节点
    </div>
  </div>

  <!-- Tool selector dialog -->
  <ToolSelectorDialog
    v-model="showToolSelector"
    @tool-selected="onToolSelected"
  />
</template>

<script setup lang="ts">
import { NodeType } from '@/types/workflow';
import { useWorkflowStore } from '@/stores/workflow';
import type { Position } from '@/types/workflow';
import { ref } from 'vue';
import ToolSelectorDialog from '../ToolSelectorDialog.vue';
import type { Tool } from '@/types/tools';

const props = defineProps<{
  visible: boolean;
  position: Position;
  clickPosition: Position;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const store = useWorkflowStore();
const showToolSelector = ref(false);

// 可用的节点类型
const availableNodeTypes = [
  NodeType.FUNCTION,
  NodeType.CONDITION,
  NodeType.END
];

// 节点类型标签
const nodeTypeLabels = {
  [NodeType.START]: '开始',
  [NodeType.FUNCTION]: '函数',
  [NodeType.CONDITION]: '条件',
  [NodeType.END]: '结束'
};

// 处理节点添加
function handleNodeAdd(type: NodeType) {
  if (type === NodeType.FUNCTION) {
    showToolSelector.value = true;
  } else {
    store.addNode(type, props.clickPosition);
  }
  emit('close');
}

// Handle tool selection
function onToolSelected(tool: Tool) {
  store.addFunctionNode(tool, props.clickPosition, tool.name);
  showToolSelector.value = false;
}
</script>

<style scoped>
.context-menu {
  position: fixed;
  background: var(--node-bg);
  /* border: 1px solid var(--node-border); */
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 160px;
  z-index: 1000;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
  color: var(--text-color);
  font-size: 14px;
  transition: background-color 0.2s;
}

.menu-item:hover {
  background-color: var(--bg-secondary);
}
</style> 
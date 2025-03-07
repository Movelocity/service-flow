<template>
  <div class="variable-selector">
    <div v-if="label" class="form-label">{{ label }}</div>
    
    <el-select
      v-model="selectedVariable"
      :placeholder="placeholder"
      class="variable-select"
      @change="onVariableSelect"
    >
      <el-option-group label="全局变量" v-if="globalVariables.length">
        <el-option
          v-for="variable in globalVariables"
          :key="`global:${variable.name}`"
          :label="variable.name"
          :value="`global:${variable.name}`"
        >
          <div class="variable-option">
            <span>{{ variable.name }}</span>
            <span class="variable-type">{{ variable.type }}</span>
          </div>
        </el-option>
      </el-option-group>

      <el-option-group label="上下文变量" v-if="contextVariables.length">
        <el-option
          v-for="variable in contextVariables"
          :key="`${variable.parent}:${variable.name}`"
          :label="`${variable.name} (from ${getNodeName(variable.parent)})`"
          :value="`${variable.parent}:${variable.name}`"
        >
          <div class="variable-option">
            <span>{{ variable.name }}</span>
            <span class="variable-source">{{ getNodeName(variable.parent) }}</span>
            <span class="variable-type">{{ variable.type }}</span>
          </div>
        </el-option>
      </el-option-group>
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { WorkflowVariable } from '@/types/workflow';
import { useWorkflowStore } from '@/stores/workflow';

const props = defineProps<{
  modelValue?: string;  // 格式: "parent:name"
  label?: string;
  placeholder?: string;
  requiredType?: string;  // 可选的类型限制
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: string): void;
}>();

const store = useWorkflowStore();

const selectedVariable = computed({
  get: () => props.modelValue || '',
  set: (value: string) => {
    emit('update:modelValue', value);
  }
});

// 获取全局变量列表
const globalVariables = computed(() => {
  return store.currentWorkflow?.globalVariables || [];
});

// 获取上下文变量列表（来自前置节点的输出）
const contextVariables = computed(() => {
  const variables: WorkflowVariable[] = [];
  const selectedNode = store.selectedNode;
  
  if (selectedNode && store.currentWorkflow) {
    // 获取所有前置节点
    const predecessors = store.getNodePredecessors(selectedNode.id);
    
    // 收集所有前置节点的输出变量
    for (const nodeId of predecessors) {
      const node = store.currentWorkflow.nodes.find(n => n.id === nodeId);
      if (node?.outputVariables) {
        variables.push(
          ...node.outputVariables.map(v => ({
            parent: node.id,
            name: v.name,
            type: v.type,
            value: undefined
          }))
        );
      }
    }
  }
  
  return variables;
});

// 获取节点名称的辅助函数
function getNodeName(nodeId: string): string {
  return store.currentWorkflow?.nodes.find(n => n.id === nodeId)?.name || nodeId;
}

// 变量选择处理函数
function onVariableSelect(value: string) {
  emit('change', value);
}
</script>

<style scoped>
.variable-selector {
  margin-bottom: 1rem;
}

.variable-select {
  width: 100%;
}

.variable-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.variable-type {
  font-size: 0.8em;
  color: var(--text-color-secondary);
  background: var(--border-color);
  padding: 2px 6px;
  border-radius: 4px;
}

.variable-source {
  font-size: 0.8em;
  color: var(--text-color-secondary);
}
</style> 
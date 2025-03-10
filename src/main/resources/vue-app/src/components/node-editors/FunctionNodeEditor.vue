<template>
  <div class="function-node-editor">
    <!-- Tool Information -->
    <div class="tool-info" v-if="selectedTool">
      <span class="tool-name">工具 {{ selectedTool.name }}</span>
      <span class="tool-description">{{ selectedTool.description }}</span>
    </div>

    <!-- Input Variables -->
    <div class="input-section" v-if="selectedTool?.inputs">
      <b>输入变量</b>
      <div v-for="(input, key) in selectedTool.inputs" :key="key" class="param-item">
        <div class="param-label-container">
          <div class="param-label">{{ key }}</div>
          <div class="param-type">{{ input.type }}</div>
        </div>
        <el-select 
          v-model="nodeInputs[key]" 
          class="input-selector"
          placeholder="选择输入来源">
          <el-option
            v-for="(_value, contextKey) in availableContext"
            :key="contextKey"
            :label="contextKey"
            :value="contextKey"
          />
        </el-select>
        <div class="param-description">{{ input.description }}</div>
      </div>
    </div>

    <!-- Output Variables -->
    <div class="output-section" v-if="selectedTool?.outputs">
      <b>输出变量</b>
      <div v-for="(output, key) in selectedTool.outputs" :key="key" class="param-item">
        <div class="param-label-container">
          <div class="param-label">{{ key }}</div>
          <div class="param-type">{{ output.type }}</div>
        </div>
        <div class="param-description">{{ output.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'

const props = defineProps<{
  nodeId: string
}>()

const workflowStore = useWorkflowStore()

// Get the current node
const currentNode = computed(() => {
  return workflowStore.currentWorkflow?.nodes.find(node => node.id === props.nodeId)
})

// Get the selected tool
const selectedTool = computed(() => {
  if (!currentNode.value?.toolName || !workflowStore.currentWorkflow?.tools) return null
  return workflowStore.currentWorkflow.tools[currentNode.value.toolName]
})

// console.log(selectedTool.value?.outputs)

// Available context for input selection
const availableContext = computed(() => {
  const context: Record<string, any> = {};
  
  // Add workflow inputs with 'global:' prefix
  if (workflowStore.currentWorkflow?.inputs) {
    Object.keys(workflowStore.currentWorkflow.inputs).forEach(key => {
      context[`global:${key}`] = workflowStore.currentWorkflow!.inputs[key];
    });
  }
  
  // Add node context
  const nodeContext = currentNode.value?.context || {};
  Object.entries(nodeContext).forEach(([key, value]) => {
    context[key] = value;
  });
  
  return context;
})

// Node inputs (for v-model binding)
const nodeInputs = ref<Record<string, string>>({})
</script>

<style scoped>
/* .function-node-editor {
  padding: 16px;
} */

.tool-info {
  margin-bottom: 20px;
}

.description {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.input-section, .output-section {
  margin-bottom: 20px;
}

.param-item {
  margin: 8px;
}

.input-selector {
  width: 100%;
  margin: 4px 0;
}

.param-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.param-label-container {
  display: flex;
  justify-content: start;
  align-items: center;
}

.param-label {
  font-weight: bold;
  margin-right: 10px;
}

.param-type {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.tool-info {
  margin: 10px 0;
  display: flex;
  flex-direction: column;
  justify-content: start;
  align-items: start;
}

.tool-name {
  font-size: 16px;
  font-weight: bold;
}

.tool-description {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}


</style> 
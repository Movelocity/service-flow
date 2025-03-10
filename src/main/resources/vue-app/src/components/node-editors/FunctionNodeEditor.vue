<template>
  <div class="function-node-editor">
    <!-- Tool Information -->
    <div class="tool-info" v-if="selectedTool">
      <h3>{{ selectedTool.name }}</h3>
      <p class="description">{{ selectedTool.description }}</p>
    </div>

    <!-- Input Variables -->
    <div class="input-section" v-if="selectedTool?.inputs">
      <h4>输入变量</h4>
      <div v-for="(input, key) in selectedTool.inputs" :key="key" class="input-item">
        <div class="input-label">{{ key }}</div>
        <el-select 
          v-model="nodeInputs[key]" 
          class="input-selector"
          placeholder="选择输入来源">
          <el-option
            v-for="(value, contextKey) in availableContext"
            :key="contextKey"
            :label="contextKey"
            :value="contextKey"
          />
        </el-select>
        <div class="input-description">{{ input.description }}</div>
      </div>
    </div>

    <!-- Output Variables -->
    <div class="output-section" v-if="selectedTool?.outputs">
      <h4>输出变量</h4>
      <div v-for="(output, key) in selectedTool.outputs" :key="key" class="output-item">
        <div class="output-label">{{ key }}</div>
        <div class="output-description">{{ output.description }}</div>
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
.function-node-editor {
  padding: 16px;
}

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

.input-item, .output-item {
  margin-bottom: 16px;
}

.input-label, .output-label {
  font-weight: bold;
  margin-bottom: 4px;
}

.input-selector {
  width: 100%;
  margin: 4px 0;
}

.input-description, .output-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
</style> 
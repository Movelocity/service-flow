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
        <div class="param-header">
          <div class="param-info">
            <span class="param-label">{{ key }}</span>
            <span class="param-type">{{ input.type }}</span>
          </div>
          <div class="input-type-select">
            <el-select 
              v-model="inputTypeMap[key]"
              class="type-selector"
              size="small">
              <el-option label="变量" value="VARIABLE" />
              <el-option label="常量" value="CONSTANT" />
            </el-select>
          </div>
        </div>
        
        <div class="input-container">
          <div class="input-field">
            <!-- Variable selector -->
            <el-select 
              v-if="inputTypeMap[key] === 'VARIABLE'"
              v-model="variableInputs[key]" 
              class="input-selector"
              placeholder="选择输入来源">
              <el-option
                v-for="value in availableContext"
                :key="value.name"
                :label="value.name"
                :value="value.name"
              />
            </el-select>
            
            <!-- Constant input based on type -->
            <template v-else>
              <el-input
                v-if="input.type.toLowerCase() === 'string'"
                v-model="constantInputs[key]"
                placeholder="输入文本"
                type="textarea"
                :autosize="{ minRows: 1, maxRows: 5 }"
                spellcheck="false"
              />
              <el-input-number
                v-else-if="input.type.toLowerCase() === 'number'"
                v-model="constantInputs[key]"
                :controls="false"
                placeholder="输入数字"
              />
              <el-select
                v-else-if="input.type.toLowerCase() === 'boolean'"
                v-model="constantInputs[key]"
                placeholder="选择布尔值"
              >
                <el-option label="是" value="true" />
                <el-option label="否" value="false" />
              </el-select>
              <el-input
                v-else
                v-model="constantInputs[key]"
                placeholder="输入值"
              />
            </template>
          </div>
          
          
        </div>
        
        <div class="param-description">{{ input.description }}</div>
      </div>
    </div>

    <!-- Output Variables -->
    <div class="output-section" v-if="selectedTool?.outputs">
      <b>输出变量</b>
      <div v-for="(output, key) in selectedTool.outputs" :key="key" class="param-item">
        <div class="row">
          <div class="param-label">{{ key }}</div>
          <div class="param-type">{{ output.type }}</div>
        </div>
        <div class="param-description">{{ output.description }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'
import { useAvailableContext } from '@/composables/useAvailableContext'

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
  const toolIndex = workflowStore.currentWorkflow.tools.findIndex(tool => tool.name === currentNode.value?.toolName);
  return workflowStore.currentWorkflow.tools[toolIndex]
})

// Available context for input selection
const availableContext = useAvailableContext(props.nodeId)

// Node inputs for variables
const variableInputs = ref<Record<string, string>>({})

// Node inputs for constants
const constantInputs = ref<Record<string, any>>({})

// Input type selection (variable or constant)
const inputTypeMap = ref<Record<string, 'VARIABLE' | 'CONSTANT'>>({})

// Initialize input types to VARIABLE by default
const initializeInputTypes = () => {
  if (selectedTool.value?.inputs) {
    for (const key in selectedTool.value.inputs) {
      if (!inputTypeMap.value[key]) {
        inputTypeMap.value[key] = 'VARIABLE'
      }
    }
  }
}

// Watch for tool changes to initialize input types
watch(selectedTool, () => {
  initializeInputTypes()
}, { immediate: true })

// Watch for input type changes to reset values
watch(inputTypeMap, (newVal, oldVal) => {
  for (const key in newVal) {
    if (newVal[key] !== oldVal?.[key]) {
      if (newVal[key] === 'VARIABLE') {
        constantInputs.value[key] = ''
      } else {
        variableInputs.value[key] = ''
      }
    }
  }
}, { deep: true })
</script>

<style scoped>

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
  padding: 8px;
  border-radius: 4px;
  /* background-color: var(--el-fill-color-light); */
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.param-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input-selector {
  width: 100%;
  margin: 4px 0;
}

.input-container {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.input-field {
  flex: 1;
}

.input-type-select {
  min-width: 90px;
}

.type-selector {
  width: 90px;
}

.param-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 6px;
  line-height: 1.4;
}

.param-label {
  font-weight: bold;
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
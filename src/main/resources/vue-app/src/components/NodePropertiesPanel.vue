<script setup lang="ts">
/**
 * NodePropertiesPanel component
 * Panel for editing the properties of a selected node
 */
import { ref, watch, computed } from 'vue'

const props = defineProps<{
  node: any | null
}>()

const emit = defineEmits<{
  'update:node': [node: any]
  'close': []
}>()

// Local copy of the node for editing
const localNode = ref<any>(null)

// Initialize local node when the selected node changes
watch(() => props.node, (newNode) => {
  if (newNode) {
    // Create a deep copy to avoid modifying the original node directly
    localNode.value = JSON.parse(JSON.stringify(newNode))
  } else {
    localNode.value = null
  }
}, { immediate: true })

// Computed property to determine if the panel should be shown
const isVisible = computed(() => !!localNode.value)

// Apply changes to the node
const applyChanges = () => {
  if (!localNode.value) return
  emit('update:node', localNode.value)
}

// Close the panel without applying changes
const closePanel = () => {
  emit('close')
}

// Get available node types
const nodeTypes = [
  { value: 'start', label: '开始节点' },
  { value: 'task', label: '任务节点' },
  { value: 'decision', label: '决策节点' },
  { value: 'end', label: '结束节点' }
]

// Get field configuration based on node type
const nodeTypeFields = computed(() => {
  if (!localNode.value) return []
  
  const type = localNode.value.type
  
  // Common fields for all node types
  const commonFields = [
    { key: 'name', label: '节点名称', type: 'text' }
  ]
  
  // Type-specific fields
  const typeSpecificFields: any[] = []
  
  switch (type) {
    case 'task':
      typeSpecificFields.push(
        { key: 'config.taskType', label: '任务类型', type: 'select', options: [
          { value: 'http', label: 'HTTP 请求' },
          { value: 'script', label: '脚本执行' },
          { value: 'service', label: '服务调用' }
        ]},
        { key: 'config.timeout', label: '超时时间 (秒)', type: 'number' }
      )
      
      // Add fields based on task type
      if (localNode.value.config?.taskType === 'http') {
        typeSpecificFields.push(
          { key: 'config.url', label: 'URL', type: 'text' },
          { key: 'config.method', label: '方法', type: 'select', options: [
            { value: 'GET', label: 'GET' },
            { value: 'POST', label: 'POST' },
            { value: 'PUT', label: 'PUT' },
            { value: 'DELETE', label: 'DELETE' }
          ]},
          { key: 'config.headers', label: '请求头', type: 'json' },
          { key: 'config.body', label: '请求体', type: 'json' }
        )
      } else if (localNode.value.config?.taskType === 'script') {
        typeSpecificFields.push(
          { key: 'config.script', label: '脚本内容', type: 'textarea' },
          { key: 'config.language', label: '脚本语言', type: 'select', options: [
            { value: 'javascript', label: 'JavaScript' },
            { value: 'groovy', label: 'Groovy' },
            { value: 'python', label: 'Python' }
          ]}
        )
      } else if (localNode.value.config?.taskType === 'service') {
        typeSpecificFields.push(
          { key: 'config.serviceName', label: '服务名称', type: 'text' },
          { key: 'config.methodName', label: '方法名称', type: 'text' },
          { key: 'config.parameters', label: '参数', type: 'json' }
        )
      }
      break
      
    case 'decision':
      typeSpecificFields.push(
        { key: 'config.condition', label: '条件表达式', type: 'textarea' },
        { key: 'config.description', label: '条件描述', type: 'text' }
      )
      break
      
    case 'start':
      typeSpecificFields.push(
        { key: 'config.inputSchema', label: '输入数据结构', type: 'json' }
      )
      break
      
    case 'end':
      typeSpecificFields.push(
        { key: 'config.outputMapping', label: '输出映射', type: 'json' }
      )
      break
  }
  
  return [...commonFields, ...typeSpecificFields]
})

// Helper function to get and set nested object properties
const getNestedValue = (obj: any, path: string) => {
  const keys = path.split('.')
  return keys.reduce((o, key) => (o || {})[key], obj)
}

const setNestedValue = (obj: any, path: string, value: any) => {
  const keys = path.split('.')
  const lastKey = keys.pop()
  const target = keys.reduce((o, key) => {
    if (o[key] === undefined) {
      o[key] = {}
    }
    return o[key]
  }, obj)
  
  if (lastKey) {
    target[lastKey] = value
  }
}

// Handle field value changes
const updateField = (key: string, value: any) => {
  if (!localNode.value) return
  setNestedValue(localNode.value, key, value)
}
</script>

<template>
  <div v-if="isVisible" class="node-properties-panel">
    <div class="panel-header">
      <h5>节点属性</h5>
      <button type="button" class="btn-close" @click="closePanel" aria-label="Close"></button>
    </div>
    
    <div class="panel-body">
      <!-- Node type selector -->
      <div class="mb-3">
        <label for="nodeType" class="form-label">节点类型</label>
        <select 
          id="nodeType" 
          class="form-select" 
          v-model="localNode.type"
        >
          <option v-for="type in nodeTypes" :key="type.value" :value="type.value">
            {{ type.label }}
          </option>
        </select>
      </div>
      
      <!-- Dynamic fields based on node type -->
      <div v-for="field in nodeTypeFields" :key="field.key" class="mb-3">
        <label :for="field.key" class="form-label">{{ field.label }}</label>
        
        <!-- Text input -->
        <input 
          v-if="field.type === 'text'" 
          :id="field.key" 
          type="text" 
          class="form-control" 
          :value="getNestedValue(localNode, field.key)"
          @input="updateField(field.key, ($event.target as HTMLInputElement).value)"
        >
        
        <!-- Number input -->
        <input 
          v-else-if="field.type === 'number'" 
          :id="field.key" 
          type="number" 
          class="form-control" 
          :value="getNestedValue(localNode, field.key)"
          @input="updateField(field.key, Number(($event.target as HTMLInputElement).value))"
        >
        
        <!-- Select input -->
        <select 
          v-else-if="field.type === 'select'" 
          :id="field.key" 
          class="form-select"
          :value="getNestedValue(localNode, field.key)"
          @change="updateField(field.key, ($event.target as HTMLSelectElement).value)"
        >
          <option v-for="option in field.options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
        
        <!-- Textarea input -->
        <textarea 
          v-else-if="field.type === 'textarea'" 
          :id="field.key" 
          class="form-control" 
          rows="5"
          :value="getNestedValue(localNode, field.key)"
          @input="updateField(field.key, ($event.target as HTMLTextAreaElement).value)"
        ></textarea>
        
        <!-- JSON input -->
        <textarea 
          v-else-if="field.type === 'json'" 
          :id="field.key" 
          class="form-control" 
          rows="5"
          :value="JSON.stringify(getNestedValue(localNode, field.key) || {}, null, 2)"
          @input="updateField(field.key, JSON.parse(($event.target as HTMLTextAreaElement).value || '{}'))"
        ></textarea>
      </div>
    </div>
    
    <div class="panel-footer">
      <button type="button" class="btn btn-secondary me-2" @click="closePanel">取消</button>
      <button type="button" class="btn btn-primary" @click="applyChanges">应用</button>
    </div>
  </div>
</template>

<style scoped>
.node-properties-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 350px;
  height: 100%;
  background-color: white;
  border-left: 1px solid #dee2e6;
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #dee2e6;
}

.panel-body {
  flex-grow: 1;
  padding: 15px;
  overflow-y: auto;
}

.panel-footer {
  padding: 15px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
}
</style> 
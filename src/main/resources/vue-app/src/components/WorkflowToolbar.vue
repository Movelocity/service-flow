<script setup lang="ts">
/**
 * WorkflowToolbar component
 * Toolbar for the workflow editor with actions for managing the workflow
 */
import { ref } from 'vue'

const emit = defineEmits<{
  'add-node': [type: string]
  'clear': []
  'auto-layout': []
  'zoom-in': []
  'zoom-out': []
  'zoom-reset': []
  'test-workflow': []
}>()

// Node types that can be added to the workflow
const nodeTypes = [
  { type: 'start', label: '开始节点', icon: 'bi-play-circle-fill', color: '#28a745' },
  { type: 'task', label: '任务节点', icon: 'bi-gear-fill', color: '#007bff' },
  { type: 'decision', label: '决策节点', icon: 'bi-diamond-fill', color: '#fd7e14' },
  { type: 'end', label: '结束节点', icon: 'bi-stop-circle-fill', color: '#dc3545' }
]

// Add a new node
const addNode = (type: string) => {
  emit('add-node', type)
}

// Clear the workflow
const clearWorkflow = () => {
  if (confirm('确定要清空工作流吗？此操作不可撤销。')) {
    emit('clear')
  }
}

// Auto-layout the workflow
const autoLayout = () => {
  emit('auto-layout')
}

// Zoom controls
const zoomIn = () => {
  emit('zoom-in')
}

const zoomOut = () => {
  emit('zoom-out')
}

const zoomReset = () => {
  emit('zoom-reset')
}

// Test the workflow
const testWorkflow = () => {
  emit('test-workflow')
}
</script>

<template>
  <div class="workflow-toolbar">
    <div class="toolbar-section">
      <div class="section-title">添加节点</div>
      <div class="node-buttons">
        <button 
          v-for="nodeType in nodeTypes" 
          :key="nodeType.type"
          class="btn node-button"
          :style="{ '--node-color': nodeType.color }"
          @click="addNode(nodeType.type)"
          :title="nodeType.label"
        >
          <i :class="['bi', nodeType.icon]"></i>
          <span>{{ nodeType.label }}</span>
        </button>
      </div>
    </div>
    
    <div class="toolbar-section">
      <div class="section-title">工作流操作</div>
      <div class="action-buttons">
        <button class="btn btn-outline-secondary btn-sm" @click="clearWorkflow" title="清空工作流">
          <i class="bi bi-trash"></i> 清空
        </button>
        <button class="btn btn-outline-secondary btn-sm" @click="autoLayout" title="自动布局">
          <i class="bi bi-grid-3x3"></i> 自动布局
        </button>
        <button class="btn btn-outline-primary btn-sm" @click="testWorkflow" title="测试工作流">
          <i class="bi bi-play-fill"></i> 测试
        </button>
      </div>
    </div>
    
    <div class="toolbar-section">
      <div class="section-title">缩放控制</div>
      <div class="zoom-buttons">
        <button class="btn btn-outline-secondary btn-sm" @click="zoomOut" title="缩小">
          <i class="bi bi-zoom-out"></i>
        </button>
        <button class="btn btn-outline-secondary btn-sm" @click="zoomReset" title="重置缩放">
          <i class="bi bi-aspect-ratio"></i>
        </button>
        <button class="btn btn-outline-secondary btn-sm" @click="zoomIn" title="放大">
          <i class="bi bi-zoom-in"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.workflow-toolbar {
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
  padding: 10px 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.toolbar-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  font-size: 0.8rem;
  font-weight: bold;
  color: #6c757d;
  text-transform: uppercase;
}

.node-buttons {
  display: flex;
  gap: 8px;
}

.node-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border: 1px solid var(--node-color, #ccc);
  border-radius: 4px;
  background-color: rgba(var(--node-color, #ccc), 0.1);
  color: var(--node-color, #333);
  transition: all 0.2s;
}

.node-button:hover {
  background-color: rgba(var(--node-color, #ccc), 0.2);
  transform: translateY(-2px);
}

.node-button i {
  font-size: 1.2rem;
  margin-bottom: 4px;
}

.node-button span {
  font-size: 0.8rem;
}

.action-buttons, .zoom-buttons {
  display: flex;
  gap: 8px;
}
</style> 
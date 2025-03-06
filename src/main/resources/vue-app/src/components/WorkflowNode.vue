<script setup lang="ts">
/**
 * WorkflowNode component
 * Represents a single node in the workflow editor
 */
import { ref, computed } from 'vue'

const props = defineProps<{
  node: {
    id: string
    type: string
    name: string
    position: { x: number, y: number }
    config: Record<string, any>
  }
  selected: boolean
}>()

const emit = defineEmits<{
  select: [nodeId: string]
  move: [nodeId: string, position: { x: number, y: number }]
  edit: [nodeId: string, data: any]
  delete: [nodeId: string]
}>()

// Node dragging state
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

// Node styles based on type and selection state
const nodeClasses = computed(() => {
  return {
    'workflow-node': true,
    'selected': props.selected,
    [`node-type-${props.node.type}`]: true
  }
})

// Handle node selection
const selectNode = (event: MouseEvent) => {
  event.stopPropagation()
  emit('select', props.node.id)
}

// Handle node dragging
const startDrag = (event: MouseEvent) => {
  if (event.button !== 0) return // Only left mouse button
  
  isDragging.value = true
  const rect = (event.target as HTMLElement).getBoundingClientRect()
  dragOffset.value = {
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  }
  
  // Add global event listeners
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDrag)
  
  // Prevent text selection during drag
  event.preventDefault()
}

const onDrag = (event: MouseEvent) => {
  if (!isDragging.value) return
  
  const canvasElement = (event.target as HTMLElement).closest('.workflow-canvas')
  if (!canvasElement) return
  
  const canvasRect = canvasElement.getBoundingClientRect()
  
  // Calculate new position relative to canvas
  const newPosition = {
    x: event.clientX - canvasRect.left - dragOffset.value.x,
    y: event.clientY - canvasRect.top - dragOffset.value.y
  }
  
  // Ensure node stays within canvas bounds
  newPosition.x = Math.max(0, Math.min(newPosition.x, canvasRect.width - 150))
  newPosition.y = Math.max(0, Math.min(newPosition.y, canvasRect.height - 100))
  
  emit('move', props.node.id, newPosition)
}

const stopDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDrag)
}

// Delete node
const deleteNode = (event: MouseEvent) => {
  event.stopPropagation()
  if (confirm('Are you sure you want to delete this node?')) {
    emit('delete', props.node.id)
  }
}

// Edit node
const editNode = (event: MouseEvent) => {
  event.stopPropagation()
  emit('edit', props.node.id, props.node)
}
</script>

<template>
  <div 
    :class="nodeClasses"
    :style="{
      left: `${node.position.x}px`,
      top: `${node.position.y}px`
    }"
    @mousedown="startDrag"
    @click="selectNode"
  >
    <div class="node-header">
      <span class="node-type">{{ node.type }}</span>
      <div class="node-actions">
        <button class="btn-edit" @click="editNode" title="Edit node">
          <i class="bi bi-pencil-fill"></i>
        </button>
        <button class="btn-delete" @click="deleteNode" title="Delete node">
          <i class="bi bi-trash-fill"></i>
        </button>
      </div>
    </div>
    <div class="node-body">
      <div class="node-name">{{ node.name }}</div>
    </div>
    <div class="node-ports">
      <div class="input-ports">
        <div class="port input-port" title="Input"></div>
      </div>
      <div class="output-ports">
        <div class="port output-port" title="Output"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.workflow-node {
  position: absolute;
  width: 150px;
  min-height: 80px;
  background-color: white;
  border: 2px solid #ccc;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: move;
  user-select: none;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.workflow-node.selected {
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  z-index: 2;
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 8px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
  border-radius: 4px 4px 0 0;
}

.node-type {
  font-size: 0.75rem;
  font-weight: bold;
  color: #666;
  text-transform: uppercase;
}

.node-actions {
  display: flex;
  gap: 4px;
}

.node-actions button {
  background: none;
  border: none;
  font-size: 0.75rem;
  padding: 2px;
  cursor: pointer;
  color: #666;
}

.node-actions button:hover {
  color: #333;
}

.node-body {
  padding: 8px;
  flex-grow: 1;
}

.node-name {
  font-weight: bold;
  margin-bottom: 4px;
  word-break: break-word;
}

.node-ports {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.port {
  width: 12px;
  height: 12px;
  background-color: #6c757d;
  border-radius: 50%;
  cursor: pointer;
  position: absolute;
}

.input-port {
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
}

.output-port {
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
}

/* Node type specific styling */
.node-type-start {
  border-color: #28a745;
}

.node-type-start .node-header {
  background-color: rgba(40, 167, 69, 0.1);
}

.node-type-end {
  border-color: #dc3545;
}

.node-type-end .node-header {
  background-color: rgba(220, 53, 69, 0.1);
}

.node-type-task {
  border-color: #007bff;
}

.node-type-task .node-header {
  background-color: rgba(0, 123, 255, 0.1);
}

.node-type-decision {
  border-color: #fd7e14;
}

.node-type-decision .node-header {
  background-color: rgba(253, 126, 20, 0.1);
}
</style> 
<script setup lang="ts">
/**
 * WorkflowConnection component
 * Represents a connection between two nodes in the workflow editor
 */
import { computed } from 'vue'

const props = defineProps<{
  connection: {
    id: string
    sourceId: string
    targetId: string
    sourcePosition: { x: number, y: number }
    targetPosition: { x: number, y: number }
    label?: string
  }
  selected: boolean
}>()

const emit = defineEmits<{
  select: [connectionId: string]
  delete: [connectionId: string]
}>()

// Calculate the path for the connection line
const path = computed(() => {
  const source = props.connection.sourcePosition
  const target = props.connection.targetPosition
  
  // Calculate control points for the bezier curve
  const dx = target.x - source.x
  const dy = target.y - source.y
  const controlPointOffset = Math.min(Math.abs(dx) * 0.5, 100)
  
  // Start point (output port of source node)
  const startX = source.x
  const startY = source.y
  
  // End point (input port of target node)
  const endX = target.x
  const endY = target.y
  
  // Control points for the bezier curve
  const controlPoint1X = startX + controlPointOffset
  const controlPoint1Y = startY
  const controlPoint2X = endX - controlPointOffset
  const controlPoint2Y = endY
  
  return `M ${startX} ${startY} C ${controlPoint1X} ${controlPoint1Y}, ${controlPoint2X} ${controlPoint2Y}, ${endX} ${endY}`
})

// Calculate the position for the connection label
const labelPosition = computed(() => {
  const source = props.connection.sourcePosition
  const target = props.connection.targetPosition
  
  return {
    x: (source.x + target.x) / 2,
    y: (source.y + target.y) / 2 - 10
  }
})

// Handle connection selection
const selectConnection = (event: MouseEvent) => {
  event.stopPropagation()
  emit('select', props.connection.id)
}

// Delete connection
const deleteConnection = (event: MouseEvent) => {
  event.stopPropagation()
  if (confirm('Are you sure you want to delete this connection?')) {
    emit('delete', props.connection.id)
  }
}
</script>

<template>
  <g class="workflow-connection" :class="{ selected }" @click="selectConnection">
    <!-- Connection path -->
    <path 
      :d="path" 
      class="connection-path" 
      fill="none" 
      stroke="#6c757d" 
      stroke-width="2"
    />
    
    <!-- Connection endpoints -->
    <circle 
      :cx="connection.sourcePosition.x" 
      :cy="connection.sourcePosition.y" 
      r="4" 
      class="connection-endpoint source"
    />
    <circle 
      :cx="connection.targetPosition.x" 
      :cy="connection.targetPosition.y" 
      r="4" 
      class="connection-endpoint target"
    />
    
    <!-- Connection label (if provided) -->
    <g v-if="connection.label" class="connection-label" :transform="`translate(${labelPosition.x}, ${labelPosition.y})`">
      <rect 
        x="-40" 
        y="-15" 
        width="80" 
        height="20" 
        rx="4" 
        ry="4" 
        class="label-background"
      />
      <text 
        x="0" 
        y="0" 
        text-anchor="middle" 
        dominant-baseline="middle" 
        class="label-text"
      >
        {{ connection.label }}
      </text>
    </g>
    
    <!-- Delete button (visible on hover/selection) -->
    <g 
      class="connection-delete" 
      :transform="`translate(${labelPosition.x + 45}, ${labelPosition.y})`"
      @click="deleteConnection"
    >
      <circle cx="0" cy="0" r="8" class="delete-background" />
      <text x="0" y="0" text-anchor="middle" dominant-baseline="middle" class="delete-icon">×</text>
    </g>
  </g>
</template>

<style scoped>
.workflow-connection {
  cursor: pointer;
}

.workflow-connection.selected .connection-path {
  stroke: #007bff;
  stroke-width: 3;
}

.connection-endpoint {
  fill: #6c757d;
}

.workflow-connection.selected .connection-endpoint {
  fill: #007bff;
}

.connection-label {
  cursor: default;
}

.label-background {
  fill: white;
  stroke: #dee2e6;
  stroke-width: 1;
}

.label-text {
  font-size: 12px;
  fill: #495057;
}

.connection-delete {
  opacity: 0;
  cursor: pointer;
  transition: opacity 0.2s;
}

.workflow-connection:hover .connection-delete,
.workflow-connection.selected .connection-delete {
  opacity: 1;
}

.delete-background {
  fill: #dc3545;
}

.delete-icon {
  fill: white;
  font-size: 14px;
  font-weight: bold;
}
</style> 
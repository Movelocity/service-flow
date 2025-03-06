<template>
  <svg class="workflow-connections" :width="width" :height="height">
    <path
      v-for="connection in connections"
      :key="`${connection.from}-${connection.to}`"
      :d="getConnectionPath(connection)"
      :class="['connection-path', { 'is-selected': connection.selected }]"
      @click="$emit('select', connection)"
    />
    <path
      v-if="tempConnection"
      :d="getTempConnectionPath()"
      class="connection-path temp"
    />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Connection {
  from: string
  to: string
  selected?: boolean
}

interface Position {
  x: number
  y: number
}

const props = defineProps<{
  connections: Connection[]
  nodes: Map<string, { position: Position }>
  width: number
  height: number
  tempConnection?: {
    fromNode: string
    fromType: 'input' | 'output'
    toPosition: Position
  }
}>()

defineEmits<{
  (e: 'select', connection: Connection): void
}>()

const getNodeConnectionPoint = (nodeId: string, type: 'input' | 'output'): Position => {
  const node = props.nodes.get(nodeId)
  if (!node) return { x: 0, y: 0 }

  const nodeWidth = 200 // Same as in CSS
  return {
    x: node.position.x + (type === 'input' ? 0 : nodeWidth),
    y: node.position.y + 30 // Approximately middle of the node
  }
}

const getConnectionPath = (connection: Connection): string => {
  const start = getNodeConnectionPoint(connection.from, 'output')
  const end = getNodeConnectionPoint(connection.to, 'input')
  
  const controlPoint1 = {
    x: start.x + 50,
    y: start.y
  }
  const controlPoint2 = {
    x: end.x - 50,
    y: end.y
  }
  
  return `M ${start.x},${start.y} C ${controlPoint1.x},${controlPoint1.y} ${controlPoint2.x},${controlPoint2.y} ${end.x},${end.y}`
}

const getTempConnectionPath = () => {
  if (!props.tempConnection) return ''
  
  const start = getNodeConnectionPoint(
    props.tempConnection.fromNode,
    props.tempConnection.fromType
  )
  const end = props.tempConnection.toPosition
  
  const controlPoint1 = {
    x: start.x + (props.tempConnection.fromType === 'output' ? 50 : -50),
    y: start.y
  }
  const controlPoint2 = {
    x: end.x - (props.tempConnection.fromType === 'output' ? -50 : 50),
    y: end.y
  }
  
  return `M ${start.x},${start.y} C ${controlPoint1.x},${controlPoint1.y} ${controlPoint2.x},${controlPoint2.y} ${end.x},${end.y}`
}
</script>

<style scoped>
.workflow-connections {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: -1;
}

.connection-path {
  fill: none;
  stroke: #6c757d;
  stroke-width: 2;
  pointer-events: stroke;
  cursor: pointer;
}

.connection-path.temp {
  stroke-dasharray: 4;
  animation: dash 1s linear infinite;
}

.connection-path.is-selected {
  stroke: #3b82f6;
  stroke-width: 3;
}

@keyframes dash {
  to {
    stroke-dashoffset: -8;
  }
}
</style> 
<script setup lang="ts">
/**
 * WorkflowCanvas component
 * The main canvas for the workflow editor where nodes and connections are displayed
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import WorkflowNode from './WorkflowNode.vue'
import WorkflowConnection from './WorkflowConnection.vue'

interface Props {
  nodes: any[]
  connections: any[]
}

const props = withDefaults(defineProps<Props>(), {
  nodes: () => [],
  connections: () => []
})

const emit = defineEmits<{
  'update:nodes': [nodes: any[]]
  'update:connections': [connections: any[]]
  'node-selected': [nodeId: string | null]
  'connection-selected': [connectionId: string | null]
  'node-added': [node: any]
}>()

// Canvas state
const canvasRef = ref<HTMLDivElement | null>(null)
const selectedNodeId = ref<string | null>(null)
const selectedConnectionId = ref<string | null>(null)
const isConnecting = ref(false)
const connectionStart = ref<{ nodeId: string, position: { x: number, y: number } } | null>(null)
const connectionEnd = ref<{ x: number, y: number } | null>(null)
const isDraggingCanvas = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const canvasOffset = ref({ x: 0, y: 0 })
const scale = ref(1)

// Computed properties for rendering
const nodesWithPositions = computed(() => {
  return props.nodes.map(node => ({
    ...node,
    position: {
      x: node.position.x + canvasOffset.value.x,
      y: node.position.y + canvasOffset.value.y
    }
  }))
})

const connectionsWithPositions = computed(() => {
  if (!props.connections || !props.nodes) return []
  
  return props.connections.map(connection => {
    const sourceNode = props.nodes.find(node => node.id === connection.sourceId)
    const targetNode = props.nodes.find(node => node.id === connection.targetId)
    
    if (!sourceNode || !targetNode) return connection
    
    return {
      ...connection,
      sourcePosition: {
        x: sourceNode.position.x + 75 + canvasOffset.value.x, // Center of node width (150/2)
        y: sourceNode.position.y + 80 + canvasOffset.value.y  // Bottom of node
      },
      targetPosition: {
        x: targetNode.position.x + 75 + canvasOffset.value.x, // Center of node width (150/2)
        y: targetNode.position.y + canvasOffset.value.y       // Top of node
      }
    }
  })
})

// Temporary connection path for drawing new connections
const temporaryConnectionPath = computed(() => {
  if (!connectionStart.value || !connectionEnd.value) return ''
  
  const start = connectionStart.value.position
  const end = connectionEnd.value
  
  // Calculate control points for the bezier curve
  const dx = end.x - start.x
  const dy = end.y - start.y
  const controlPointOffset = Math.min(Math.abs(dx) * 0.5, 100)
  
  // Control points for the bezier curve
  const controlPoint1X = start.x + controlPointOffset
  const controlPoint1Y = start.y
  const controlPoint2X = end.x - controlPointOffset
  const controlPoint2Y = end.y
  
  return `M ${start.x} ${start.y} C ${controlPoint1X} ${controlPoint1Y}, ${controlPoint2X} ${controlPoint2Y}, ${end.x} ${end.y}`
})

// Canvas event handlers
const handleCanvasClick = (event: MouseEvent) => {
  // If we're not in the middle of creating a connection
  if (!isConnecting.value) {
    // Deselect any selected node or connection
    selectedNodeId.value = null
    selectedConnectionId.value = null
    emit('node-selected', null)
    emit('connection-selected', null)
  }
}

const startDraggingCanvas = (event: MouseEvent) => {
  // Only start dragging if it's a right-click or middle-click
  if (event.button !== 2 && event.button !== 1) return
  
  isDraggingCanvas.value = true
  dragStart.value = { x: event.clientX, y: event.clientY }
  
  // Prevent context menu on right-click
  if (event.button === 2) {
    event.preventDefault()
  }
}

const dragCanvas = (event: MouseEvent) => {
  if (!isDraggingCanvas.value) return
  
  const dx = event.clientX - dragStart.value.x
  const dy = event.clientY - dragStart.value.y
  
  canvasOffset.value = {
    x: canvasOffset.value.x + dx,
    y: canvasOffset.value.y + dy
  }
  
  dragStart.value = { x: event.clientX, y: event.clientY }
}

const stopDraggingCanvas = () => {
  isDraggingCanvas.value = false
}

// Node event handlers
const handleNodeSelected = (nodeId: string) => {
  selectedNodeId.value = nodeId
  selectedConnectionId.value = null
  emit('node-selected', nodeId)
  emit('connection-selected', null)
}

const handleNodeMoved = (nodeId: string, newPosition: { x: number, y: number }) => {
  const updatedNodes = props.nodes.map(node => {
    if (node.id === nodeId) {
      return {
        ...node,
        position: {
          x: newPosition.x - canvasOffset.value.x,
          y: newPosition.y - canvasOffset.value.y
        }
      }
    }
    return node
  })
  
  emit('update:nodes', updatedNodes)
}

const handleNodeEdited = (nodeId: string, data: any) => {
  // This would typically open a modal or sidebar for editing the node
  console.log('Edit node', nodeId, data)
}

const handleNodeDeleted = (nodeId: string) => {
  // Remove the node
  const updatedNodes = props.nodes.filter(node => node.id !== nodeId)
  
  // Remove any connections to/from this node
  const updatedConnections = props.connections.filter(
    conn => conn.sourceId !== nodeId && conn.targetId !== nodeId
  )
  
  emit('update:nodes', updatedNodes)
  emit('update:connections', updatedConnections)
  
  if (selectedNodeId.value === nodeId) {
    selectedNodeId.value = null
    emit('node-selected', null)
  }
}

// Connection event handlers
const handleConnectionSelected = (connectionId: string) => {
  selectedConnectionId.value = connectionId
  selectedNodeId.value = null
  emit('connection-selected', connectionId)
  emit('node-selected', null)
}

const handleConnectionDeleted = (connectionId: string) => {
  const updatedConnections = props.connections.filter(conn => conn.id !== connectionId)
  emit('update:connections', updatedConnections)
  
  if (selectedConnectionId.value === connectionId) {
    selectedConnectionId.value = null
    emit('connection-selected', null)
  }
}

// Add a new node at the specified position
const addNode = (type: string, x: number, y: number) => {
  const newNode = {
    id: 'node_' + Date.now(),
    type,
    name: `New ${type.charAt(0).toUpperCase() + type.slice(1)} Node`,
    position: {
      x: x - canvasOffset.value.x,
      y: y - canvasOffset.value.y
    },
    config: {}
  }
  
  emit('node-added', newNode)
}

// Zoom handling
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()
  
  const delta = event.deltaY > 0 ? -0.1 : 0.1
  const newScale = Math.max(0.5, Math.min(2, scale.value + delta))
  
  // Calculate the point to zoom towards (mouse position)
  const rect = canvasRef.value?.getBoundingClientRect()
  if (!rect) return
  
  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top
  
  // Calculate new offset to zoom towards mouse position
  const scaleFactor = newScale / scale.value
  const newOffsetX = mouseX - (mouseX - canvasOffset.value.x) * scaleFactor
  const newOffsetY = mouseY - (mouseY - canvasOffset.value.y) * scaleFactor
  
  scale.value = newScale
  canvasOffset.value = { x: newOffsetX, y: newOffsetY }
}

// Context menu for adding nodes
const showContextMenu = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })

const handleContextMenu = (event: MouseEvent) => {
  event.preventDefault()
  
  // Show context menu at mouse position
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  showContextMenu.value = true
  
  // Add event listeners to close the context menu when clicking elsewhere
  document.addEventListener('click', closeContextMenu)
  document.addEventListener('contextmenu', closeContextMenu)
}

const closeContextMenu = () => {
  showContextMenu.value = false
  document.removeEventListener('click', closeContextMenu)
  document.removeEventListener('contextmenu', closeContextMenu)
}

const addNodeFromContextMenu = (type: string) => {
  const rect = canvasRef.value?.getBoundingClientRect()
  if (!rect) return
  
  const x = contextMenuPosition.value.x - rect.left
  const y = contextMenuPosition.value.y - rect.top
  
  addNode(type, x, y)
  closeContextMenu()
}

// Event listeners
onMounted(() => {
  if (canvasRef.value) {
    canvasRef.value.addEventListener('wheel', handleWheel, { passive: false })
  }
  
  // Prevent browser context menu
  document.addEventListener('contextmenu', (e) => e.preventDefault())
})

onUnmounted(() => {
  if (canvasRef.value) {
    canvasRef.value.removeEventListener('wheel', handleWheel)
  }
  
  document.removeEventListener('click', closeContextMenu)
  document.removeEventListener('contextmenu', closeContextMenu)
})
</script>

<template>
  <div 
    ref="canvasRef"
    class="workflow-canvas" 
    @click="handleCanvasClick"
    @mousedown="startDraggingCanvas"
    @mousemove="dragCanvas"
    @mouseup="stopDraggingCanvas"
    @contextmenu="handleContextMenu"
  >
    <!-- SVG layer for connections -->
    <svg class="connections-layer" width="100%" height="100%">
      <!-- Existing connections -->
      <WorkflowConnection
        v-for="connection in connectionsWithPositions"
        :key="connection.id"
        :connection="connection"
        :selected="selectedConnectionId === connection.id"
        @select="handleConnectionSelected"
        @delete="handleConnectionDeleted"
      />
      
      <!-- Temporary connection being drawn -->
      <path 
        v-if="isConnecting && connectionStart && connectionEnd"
        :d="temporaryConnectionPath"
        class="temporary-connection"
        stroke="#007bff"
        stroke-width="2"
        stroke-dasharray="5,5"
        fill="none"
      />
    </svg>
    
    <!-- Nodes layer -->
    <div class="nodes-layer">
      <WorkflowNode
        v-for="node in nodesWithPositions"
        :key="node.id"
        :node="node"
        :selected="selectedNodeId === node.id"
        @select="handleNodeSelected"
        @move="handleNodeMoved"
        @edit="handleNodeEdited"
        @delete="handleNodeDeleted"
      />
    </div>
    
    <!-- Context menu for adding nodes -->
    <div 
      v-if="showContextMenu" 
      class="context-menu"
      :style="{
        left: `${contextMenuPosition.x}px`,
        top: `${contextMenuPosition.y}px`
      }"
    >
      <div class="context-menu-item" @click="addNodeFromContextMenu('start')">
        Add Start Node
      </div>
      <div class="context-menu-item" @click="addNodeFromContextMenu('task')">
        Add Task Node
      </div>
      <div class="context-menu-item" @click="addNodeFromContextMenu('decision')">
        Add Decision Node
      </div>
      <div class="context-menu-item" @click="addNodeFromContextMenu('end')">
        Add End Node
      </div>
    </div>
  </div>
</template>

<style scoped>
.workflow-canvas {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #f8f9fa;
  background-image: 
    linear-gradient(rgba(0, 0, 0, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 0, 0, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  overflow: hidden;
  cursor: default;
}

.connections-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.connections-layer * {
  pointer-events: auto;
}

.nodes-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.temporary-connection {
  pointer-events: none;
}

.context-menu {
  position: fixed;
  background-color: white;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  min-width: 150px;
}

.context-menu-item {
  padding: 8px 12px;
  cursor: pointer;
}

.context-menu-item:hover {
  background-color: #f8f9fa;
}
</style> 
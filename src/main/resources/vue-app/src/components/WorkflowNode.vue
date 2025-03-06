<template>
  <div 
    :class="['node', `node-${node.type.toLowerCase()}`]"
    :style="nodeStyle"
    @mousedown="onMouseDown"
    @dblclick="$emit('edit', node)"
  >
    <div class="node-header">{{ node.name }}</div>
    <div class="node-type">{{ node.type }}</div>
    <div 
      class="connection-point input"
      @mousedown.stop="startConnection('input', $event)"
    ></div>
    <div 
      class="connection-point output"
      @mousedown.stop="startConnection('output', $event)"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  node: {
    id: string
    name: string
    type: string
    position: { x: number; y: number }
  }
}>()

const emit = defineEmits<{
  (e: 'dragStart', event: MouseEvent): void
  (e: 'edit', node: any): void
  (e: 'connectionStart', data: { nodeId: string, type: 'input' | 'output', event: MouseEvent }): void
}>()

const nodeStyle = computed(() => ({
  left: `${props.node.position.x}px`,
  top: `${props.node.position.y}px`,
  cursor: 'move'
}))

const onMouseDown = (event: MouseEvent) => {
  if (event.button === 0) { // Left click only
    emit('dragStart', event)
  }
}

const startConnection = (type: 'input' | 'output', event: MouseEvent) => {
  emit('connectionStart', { 
    nodeId: props.node.id, 
    type, 
    event 
  })
}
</script>

<style scoped>
.node {
  border: 2px solid #6c757d;
  border-radius: 6px;
  padding: 10px;
  background-color: white;
  width: 200px;
  position: absolute;
  user-select: none;
}

.connection-point {
  width: 10px;
  height: 10px;
  background: #6c757d;
  border-radius: 50%;
  position: absolute;
  cursor: pointer;
}

.connection-point.input {
  top: 50%;
  left: -5px;
  transform: translateY(-50%);
}

.connection-point.output {
  top: 50%;
  right: -5px;
  transform: translateY(-50%);
}

.node-start { border-color: #10b981; }
.node-end { border-color: #ef4444; }
.node-condition { border-color: #f59e0b; }
.node-function { border-color: #3b82f6; }
</style> 
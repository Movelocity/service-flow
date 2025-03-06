<template>
  <div
    :class="[
      'workflow-node',
      node.type.toLowerCase(),
      { selected: isSelected }
    ]"
    :style="nodeStyle"
    @mousedown="onNodeMouseDown"
    @click.stop="onNodeClick"
  >
    <div class="node-header">
      {{ node.name }}
      <small class="node-type">{{ node.type }}</small>
    </div>
    
    <div class="node-content">
      <template v-if="node.type === 'CONDITION'">
        <div class="condition-expression">
          {{ node.parameters.condition || '未设置条件' }}
        </div>
      </template>
      <template v-else-if="node.type === 'FUNCTION'">
        <div class="function-info">
          {{ node.parameters.function || '未设置函数' }}
        </div>
      </template>
    </div>

    <!-- 连接点 -->
    <div
      v-if="node.type !== 'END'"
      class="connection-point output"
      @mousedown.stop="onOutputPointMouseDown"
      @mouseup.stop="onOutputPointMouseUp"
    />
    <div
      v-if="node.type !== 'START'"
      class="connection-point input"
      @mousedown.stop="onInputPointMouseDown"
      @mouseup.stop="onInputPointMouseUp"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Node } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';

const props = defineProps<{
  node: Node;
  isSelected: boolean;
}>();

const emit = defineEmits<{
  (e: 'startConnection', nodeId: string, isOutput: boolean): void;
  (e: 'endConnection', nodeId: string, isOutput: boolean): void;
}>();

const store = useWorkflowStore();

// 添加拖拽状态标记
let isDragging = false;

const nodeStyle = computed(() => ({
  transform: `translate(${props.node.position.x}px, ${props.node.position.y}px)`
}));

// 节点拖拽
function onNodeMouseDown(event: MouseEvent) {
  // 防止触发画布的拖拽
  event.stopPropagation();
  
  const canvasState = store.editorState.canvasState;
  const scale = canvasState.scale;
  
  // 计算鼠标相对于节点的初始位置（考虑缩放）
  const startX = (event.clientX - canvasState.position.x) / scale - props.node.position.x;
  const startY = (event.clientY - canvasState.position.y) / scale - props.node.position.y;
  
  // 设置拖拽标记
  isDragging = false;
  
  function onMouseMove(e: MouseEvent) {
    // 设置拖拽标记
    isDragging = true;
    
    // 计算新位置（考虑缩放）
    const newX = (e.clientX - canvasState.position.x) / scale - startX;
    const newY = (e.clientY - canvasState.position.y) / scale - startY;
    
    // 更新节点位置
    store.updateNode(props.node.id, {
      position: { x: newX, y: newY }
    });
  }
  
  function onMouseUp() {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  }
  
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

// 节点选择
function onNodeClick() {
  // 只有在没有拖拽时才选中节点
  if (!isDragging) {
    store.setSelectedNode(props.node.id);
  }
}

// 连接点事件处理
function onOutputPointMouseDown() {
  emit('startConnection', props.node.id, true);
}

function onOutputPointMouseUp() {
  emit('endConnection', props.node.id, true);
}

function onInputPointMouseDown() {
  emit('startConnection', props.node.id, false);
}

function onInputPointMouseUp() {
  emit('endConnection', props.node.id, false);
}
</script>

<style scoped>
.workflow-node {
  position: absolute;
  width: 200px;
  min-height: 80px;
  background: var(--node-bg);
  border: 2px solid var(--node-border);
  border-radius: 6px;
  padding: 10px;
  cursor: move;
  user-select: none;
  box-shadow: var(--card-shadow);
  transition: border-color 0.2s, box-shadow 0.2s;
  color: var(--text-color);
}

.workflow-node:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.workflow-node.selected {
  border-color: var(--node-selected);
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.workflow-node.start {
  border-color: var(--node-start);
}

.workflow-node.end {
  border-color: var(--node-end);
}

.workflow-node.condition {
  border-color: var(--node-condition);
}

.workflow-node.function {
  border-color: var(--node-function);
}

.node-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  font-weight: bold;
}

.node-type {
  font-size: 0.8em;
  opacity: 0.7;
}

.node-content {
  font-size: 0.9em;
}

.condition-expression {
  color: var(--text-color);
  opacity: 0.8;
  font-style: italic;
}

.function-info {
  color: var(--text-color);
  opacity: 0.8;
}

.connection-point {
  position: absolute;
  width: 10px;
  height: 10px;
  background: var(--connection-point);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s, background-color 0.2s;
}

.connection-point:hover {
  transform: scale(1.2);
  background-color: var(--connection-point-hover);
}

.connection-point.input {
  left: -5px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output {
  right: -5px;
  top: 50%;
  transform: translateY(-50%);
}

.connection-point.output:hover {
  transform: translateY(-50%) scale(1.2);
}

.connection-point.input:hover {
  transform: translateY(-50%) scale(1.2);
}
</style> 
<template>
  <div
    :class="[
      'workflow-node',
      node.type.toLowerCase(),
      { selected: isSelected },
      { 'running': isRunning }
    ]"
    :style="nodeStyle"
    @mousedown="onNodeMouseDown"
    @click.stop="onNodeClick"
  >
    <div class="node-header">
      <div class="node-title">
        <NodeIcon :type="node.type" :size="24" />
        <span class="node-name">{{ node.name }}</span>
      </div>
    </div>
    
    <div class="node-content">
      <template v-if="node.type === 'CONDITION' && node.conditions">
        <div v-for="condition in node.conditions" class="condition-expression">
          {{ condition.hint || "ELSE" }}
        </div>
      </template>
      <template v-else>
        <div class="function-info">
          {{ node.description }}
        </div>
      </template>
    </div>

    <!-- 连接点 -->
    <template v-if="node.type === 'CONDITION' && node.conditions">
      <!-- 多个输出连接点，和条件提示对齐 -->
      <div 
        v-for="(_condition, index) in node.conditions" 
        :key="index"
        class="connection-point output"
        :class="'case-' + (index + 1)"
        :style="{
          right: '-5px',
          /* 将连接点放在条件提示的同一垂直位置 */
          top: `${70 + (index * 24)}px`, 
          transform: 'translateY(-50%)'
        }"
        @mousedown.stop="(e) => onOutputPointMouseDown(e, isEmptyLastCondition(index) ? 'else' : 'case' + (index + 1))"
        @mouseup.stop="(e) => onOutputPointMouseUp(e, isEmptyLastCondition(index) ? 'else' : 'case' + (index + 1))"
      >
        <!-- 添加标签以指示此连接点表示哪个条件 -->
        <div class="connection-point-label">
          {{ isEmptyLastCondition(index) ? 'ELSE' : 'Case ' + (index + 1) }}
        </div>
      </div>
    </template>
    <div
      v-else-if="node.type !== 'END'"
      class="connection-point output"
      @mousedown.stop="(e) => onOutputPointMouseDown(e, 'default')"
      @mouseup.stop="(e) => onOutputPointMouseUp(e, 'default')"
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
import type { Node } from '@/types/workflow';
import { useWorkflowStore } from '@/stores/workflow';
import { useDebugStore } from '@/stores/debug';
import NodeIcon from '@/components/common/NodeIcon.vue';

const props = defineProps<{
  node: Node;
  isSelected: boolean;
}>();

const emit = defineEmits<{
  (e: 'startConnection', nodeId: string, isOutput: boolean, event: MouseEvent, condition?: string): void;
  (e: 'endConnection', nodeId: string, isOutput: boolean, condition?: string): void;
}>();

const store = useWorkflowStore();
const debugStore = useDebugStore();

// 检查当前节点是否正在执行
const isRunning = computed(() => {
  console.log("NodeElem", debugStore.runningNodeId, props.node.id)
  return debugStore.runningNodeId === props.node.id;
});

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
    // 只有在移动超过一定距离时才认为是拖拽
    if (!isDragging) {
      const dx = e.clientX - event.clientX;
      const dy = e.clientY - event.clientY;
      if (Math.sqrt(dx * dx + dy * dy) > 3) {
        isDragging = true;
      }
    }
    
    if (isDragging) {
      // 计算新位置（考虑缩放）
      const newX = (e.clientX - canvasState.position.x) / scale - startX;
      const newY = (e.clientY - canvasState.position.y) / scale - startY;
      
      // 更新节点位置
      store.updateNodePosition(props.node.id, { x: newX, y: newY });
    }
  }
  
  function onMouseUp() {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  }
  
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

// 单独处理点击事件
function onNodeClick(event: MouseEvent) {
  // 阻止事件冒泡
  event.stopPropagation();
  
  // 如果没有拖拽，则选中节点
  if (!isDragging) {
    store.selectNode(props.node.id);

    console.log("NodeElem", store.selectedNode?.context)
  }
}

// 连接点事件处理
function onOutputPointMouseDown(e: MouseEvent, condition: string = 'default') {
  emit('startConnection', props.node.id, true, e, condition);
}

function onOutputPointMouseUp(_e: MouseEvent, condition: string = 'default') {
  emit('endConnection', props.node.id, true, condition);
}

function onInputPointMouseDown(e: MouseEvent) {
  emit('startConnection', props.node.id, false, e);
}

function onInputPointMouseUp(_e: MouseEvent) {
  emit('endConnection', props.node.id, false);
}

// Check if this is the last condition and if it's empty (for ELSE branch)
function isEmptyLastCondition(index: number): boolean {
  if (!props.node.conditions || props.node.conditions.length === 0) return false;
  return index === props.node.conditions.length - 1 && 
         (!props.node.conditions[index].conditions || props.node.conditions[index].conditions.length === 0);
}
</script>

<style scoped>
.workflow-node {
  position: absolute;
  width: 200px;
  min-height: 80px;
  background: var(--node-bg);
  box-shadow: 0 0 4px var(--card-shadow);
  border-radius: 6px;
  padding: 10px;
  cursor: grab;
  user-select: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  color: var(--text-color);
  border: 2px solid transparent;
}

.workflow-node.selected {
  border-color: var(--node-selected);
}

.workflow-node:hover {
  box-shadow: 0 0 18px var(--card-shadow);
}

/* 运行中的节点样式 */
.workflow-node.running {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 12px var(--el-color-primary-light-3);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 8px var(--el-color-primary-light-5);
  }
  50% {
    box-shadow: 0 0 15px var(--el-color-primary);
  }
  100% {
    box-shadow: 0 0 8px var(--el-color-primary-light-5);
  }
}

/* 运行中的选中节点样式 */
.workflow-node.selected.running {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 12px var(--el-color-primary);
  animation: pulse-selected 1.5s infinite;
}

@keyframes pulse-selected {
  0% {
    box-shadow: 0 0 8px var(--el-color-primary);
  }
  50% {
    box-shadow: 0 0 18px var(--el-color-primary);
  }
  100% {
    box-shadow: 0 0 8px var(--el-color-primary);
  }
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
  margin: 2px;
  border-radius: 4px;
  padding: 2px 4px;
  background-color: var(--card-hint);
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
  /* Default position for non-condition nodes */
  top: 50%;
  transform: translateY(-50%);
}

/* Position for the label of connection points */
.connection-point-label {
  position: absolute;
  right: 15px;
  top: 0;
  font-size: 10px;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.2s;
}

.connection-point:hover .connection-point-label {
  opacity: 1;
}

/* Hide color distinction between case types - as per requirement #1 */
.connection-point.output.case-1,
.connection-point.output.case-2,
.connection-point.output.case-3,
.connection-point.output.case-4 {
  background-color: var(--connection-point);
}

.connection-point.output.case-1:hover,
.connection-point.output.case-2:hover,
.connection-point.output.case-3:hover,
.connection-point.output.case-4:hover {
  background-color: var(--connection-point-hover);
  transform: scale(1.2);
}

.node-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-name {
  margin-left: 4px;
}
</style> 
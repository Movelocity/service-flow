<template>
  <svg
    class="connection-container"
    :style="{
      position: 'absolute',
      left: containerPosition.x + 'px',
      top: containerPosition.y + 'px',
      width: containerSize.width + 'px',
      height: containerSize.height + 'px',
      overflow: 'visible'
    }"
  >
    <path
      :class="['connection-line', { selected: isSelected }]"
      :d="pathData"
      @click.stop="onConnectionClick"
    />
    <text
      v-if="connection.condition"
      :x="labelPosition.x"
      :y="labelPosition.y"
      class="connection-label"
      @click.stop="onConnectionClick"
    >
      {{ connection.condition }}
    </text>
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Connection, Node, Position } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';
import { generateBezierPath, calculateConnectionPoint } from '../utils/canvas';

const props = defineProps<{
  connection: Connection;
  sourceNode: Node;
  targetNode: Node;
  isSelected: boolean;
}>();

const store = useWorkflowStore();

// 计算连接线的起点和终点
const startPoint = computed<Position>(() => {
  return calculateConnectionPoint(
    props.sourceNode.position,
    200, // nodeWidth from CSS variables
    80,  // nodeHeight from CSS variables
    false // isInput
  );
});

const endPoint = computed<Position>(() => {
  return calculateConnectionPoint(
    props.targetNode.position,
    200, // nodeWidth
    80,  // nodeHeight
    true  // isInput
  );
});

// 生成SVG路径
const pathData = computed(() => {
  return generateBezierPath(startPoint.value, endPoint.value);
});

// 计算连接标签的位置（在曲线中点）
const labelPosition = computed(() => {
  return {
    x: (startPoint.value.x + endPoint.value.x) / 2,
    y: (startPoint.value.y + endPoint.value.y) / 2 - 10
  };
});

// 计算SVG容器的位置和大小
const containerPosition = computed(() => ({
  x: Math.min(startPoint.value.x, endPoint.value.x),
  y: Math.min(startPoint.value.y, endPoint.value.y)
}));

const containerSize = computed(() => ({
  width: Math.abs(endPoint.value.x - startPoint.value.x),
  height: Math.abs(endPoint.value.y - startPoint.value.y)
}));

// 点击事件处理
function onConnectionClick() {
  // TODO: 实现连接的选择和编辑功能
  console.log('Connection clicked:', props.connection);
}
</script>

<style scoped>
.connection-container {
  pointer-events: none;
  z-index: 1;
}

.connection-line {
  pointer-events: all;
  cursor: pointer;
}

.connection-label {
  pointer-events: all;
  font-size: 12px;
  fill: #495057;
  text-anchor: middle;
  cursor: pointer;
  user-select: none;
}

.connection-label:hover {
  fill: var(--node-selected);
}
</style> 
<template>
  <svg
    class="connection-container"
    :style="{
      position: 'absolute',
      left: '0',
      top: '0',
      width: '100%',
      height: '100%',
      overflow: 'visible',
      pointerEvents: 'none'
    }"
  >
    <path
      :class="['connection-line', { selected: isSelected }]"
      :d="pathData"
      :style="{
        stroke: getConnectionColor(sourceNode.type),
        strokeWidth: isSelected ? '3' : '2'
      }"
      @click.stop="onConnectionClick"
      @mouseover="isHovered = true"
      @mouseleave="isHovered = false"
      style="pointer-events: all;"
    />

    <marker
      :id="'arrow-' + connection.id"
      viewBox="0 0 10 10"
      refX="5"
      refY="5"
      markerWidth="6"
      markerHeight="6"
      orient="auto-start-reverse"
    >
      <path
        d="M 0 0 L 10 5 L 0 10 z"
        :fill="getConnectionColor(sourceNode.type)"
      />
    </marker>

    <text
      v-if="connection.condition"
      :x="labelPosition.x"
      :y="labelPosition.y"
      :class="['connection-label', { selected: isSelected }]"
      @click.stop="onConnectionClick"
      style="pointer-events: all;"
    >
      {{ connection.condition }}
    </text>

    <g
      v-if="isHovered || isSelected"
      :transform="'translate(' + deleteButtonPosition.x + ',' + deleteButtonPosition.y + ')'"
      class="delete-button"
      @click.stop="onDeleteClick"
    >
      <circle r="8" fill="white" stroke="#dc3545" />
      <text x="0" y="0" dy=".3em" fill="#dc3545">×</text>
    </g>
  </svg>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { Connection, Node, Position } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';
import { generateBezierPath, calculateConnectionPoint } from '../utils/canvas';
import { NodeType } from '../types/workflow';

const props = defineProps<{
  connection: Connection;
  sourceNode: Node;
  targetNode: Node;
  isSelected: boolean;
  scale: number;
  canvasPosition: Position;
}>();

const store = useWorkflowStore();
const isHovered = ref(false);

function getConnectionColor(nodeType: NodeType): string {
  switch (nodeType) {
    case NodeType.START:
      return '#28a745';
    case NodeType.CONDITION:
      return '#ffc107';
    case NodeType.FUNCTION:
      return '#17a2b8';
    case NodeType.END:
      return '#dc3545';
    default:
      return '#6c757d';
  }
}

const startPoint = computed<Position>(() => {
  const point = calculateConnectionPoint(
    props.sourceNode.position,
    200, // nodeWidth
    80,  // nodeHeight
    false // isInput
  );
  
  // 考虑画布缩放和位置
  return {
    x: point.x * props.scale,
    y: point.y * props.scale
  };
});

const endPoint = computed<Position>(() => {
  const point = calculateConnectionPoint(
    props.targetNode.position,
    200, // nodeWidth
    80,  // nodeHeight
    true  // isInput
  );
  
  // 考虑画布缩放和位置
  return {
    x: point.x * props.scale,
    y: point.y * props.scale
  };
});

const pathData = computed(() => {
  return generateBezierPath(startPoint.value, endPoint.value);
});

const labelPosition = computed(() => {
  return {
    x: (startPoint.value.x + endPoint.value.x) / 2,
    y: (startPoint.value.y + endPoint.value.y) / 2 - 10
  };
});

const deleteButtonPosition = computed(() => {
  return {
    x: (startPoint.value.x + endPoint.value.x) / 2,
    y: (startPoint.value.y + endPoint.value.y) / 2 + 10
  };
});

function onConnectionClick() {
  store.selectConnection(props.connection.id);
}

function onDeleteClick() {
  store.deleteConnection(props.connection.id);
}
</script>

<style scoped>
.connection-container {
  pointer-events: none;
  z-index: 1;
}

.connection-line {
  fill: none;
  stroke-linecap: round;
  pointer-events: stroke;
  cursor: pointer;
  transition: stroke-width 0.2s;
}

.connection-line:hover {
  stroke-width: 3;
}

.connection-line.selected {
  stroke-width: 3;
  filter: drop-shadow(0 0 3px rgba(0, 123, 255, 0.5));
}

.connection-label {
  font-size: 12px;
  fill: #495057;
  text-anchor: middle;
  cursor: pointer;
  user-select: none;
  pointer-events: all;
}

.connection-label:hover {
  fill: #0056b3;
}

.connection-label.selected {
  fill: #0056b3;
  font-weight: bold;
}

.delete-button {
  cursor: pointer;
  pointer-events: all;
}

.delete-button text {
  font-size: 16px;
  text-anchor: middle;
  font-weight: bold;
  pointer-events: none;
}

.delete-button:hover circle {
  fill: #dc3545;
}

.delete-button:hover text {
  fill: white;
}
</style> 
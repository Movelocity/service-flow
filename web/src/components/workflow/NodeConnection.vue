<template>
  <g
    class="connection-container"
    :class="{ selected: isSelected }"
  >
    <path
      :class="['connection-line', { selected: isSelected }]"
      :d="path"
      :style="{
        stroke: getNodeColor(sourceNode.type),
        strokeWidth: isSelected ? '3' : '2'
      }"
      @click.stop="onConnectionClick"
    />

    <!-- 箭头标记 -->
    <marker
      :id="'arrow-' + sourceNode.id + '-' + condition"
      viewBox="0 0 10 10"
      refX="5"
      refY="5"
      markerWidth="6"
      markerHeight="6"
      orient="auto-start-reverse"
    >
      <path
        d="M 0 0 L 10 5 L 0 10 z"
        :fill="getNodeColor(sourceNode.type)"
      />
    </marker>

    <!-- 条件标签 -->
    <g
      v-if="condition !== 'default'"
      :transform="'translate(' + labelPosition.x + ',' + labelPosition.y + ')'"
    >
      <rect
        :class="['connection-label', { selected: isSelected }]"
        @click.stop="onConnectionClick"
        :x="-getLabelWidth(condition) / 2"
        y="-10"
        :width="getLabelWidth(condition)"
        height="20"
        rx="4"
        :style="{ fill: getLabelColor(sourceNode.type, condition) }"
      />
      <text
        text-anchor="middle"
        dominant-baseline="middle"
        :style="{ fill: '#FFFFFF' }"
      >
        {{ getDisplayLabel(condition) }}
      </text>
    </g>
  </g>
</template>

<script setup lang="ts">
import { computed, type PropType } from 'vue';
import type { Node } from '@/types/workflow';
import { generateBezierPath } from '@/utils/canvas';
import { getNodeColor, getLabelColor, getDisplayLabel, getLabelWidth } from '@/utils/nodeColors';
import { useWorkflowStore } from '@/stores/workflow';
import { PositionManager, PortType } from '@/utils/PositionManager';

const props = defineProps({
  sourceNode: {
    type: Object as PropType<Node>,
    required: true
  },
  targetNode: {
    type: Object as PropType<Node>,
    required: true
  },
  condition: {
    type: String,
    required: true
  },
  isSelected: {
    type: Boolean,
    default: false
  }
});

const store = useWorkflowStore();

// 计算连接路径
const path = computed(() => {
  const sourcePoint = PositionManager.getPortPosition(props.sourceNode, PortType.OUTPUT, props.condition);
  // console.log(props.sourceNode, props.condition, 'edge', sourcePoint);
  const targetPoint = PositionManager.getPortPosition(props.targetNode, PortType.INPUT);
  
  return generateBezierPath(sourcePoint, targetPoint);
});

// 计算标签位置
const labelPosition = computed(() => {
  return PositionManager.getConnectionLabelPosition(props.sourceNode, props.targetNode, props.condition);
});

// 点击连接线
function onConnectionClick() {
  store.selectConnection(props.sourceNode.id, props.condition);
}
</script>

<style scoped>
.connection-container {
  pointer-events: all;
  cursor: pointer;
}

.connection-line {
  fill: none;
  stroke-width: 2;
  transition: stroke-width 0.2s;
}

.connection-line:hover {
  stroke-width: 3;
}

.connection-line.selected {
  stroke-width: 3;
}

.connection-label {
  fill: white;
  stroke: #666;
  stroke-width: 1;
  transition: stroke-width 0.2s;
}

.connection-label:hover {
  stroke-width: 2;
}

.connection-label.selected {
  stroke-width: 2;
  stroke: #1976D2;
}

.connection-label text {
  font-size: 12px;
  font-weight: 500;
  pointer-events: none;
}
</style> 
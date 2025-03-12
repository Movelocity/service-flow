<template>
  <g
    class="connection-container"
    :class="{ selected: isSelected }"
  >
    <path
      :class="['connection-line', { selected: isSelected }]"
      :d="path"
      :style="{
        stroke: getConnectionColor(sourceNode.type),
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
        :fill="getConnectionColor(sourceNode.type)"
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
        :x="getLabelX(condition)"
        y="-10"
        :width="getLabelWidth(condition)"
        height="20"
        rx="4"
        :style="{ fill: getLabelColor(condition) }"
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
import { NodeType } from '@/types/workflow';
import { generateBezierPath, calculateConnectionPoint } from '@/utils/canvas';
import { useWorkflowStore } from '@/stores/workflow';

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
  let sourcePoint;
  
  // For condition nodes, calculate sourcePoint based on the condition
  if (props.sourceNode.type === NodeType.CONDITION && props.sourceNode.conditions) {
    let index = -1;
    if (props.condition.startsWith('case')) {
      index = parseInt(props.condition.slice(4)) - 1;
    } else if (props.condition === 'else' && props.sourceNode.conditions.length > 0) {
      index = props.sourceNode.conditions.length - 1;
    }
    
    if (index >= 0 && index < props.sourceNode.conditions.length) {
      sourcePoint = {
        x: props.sourceNode.position.x + 200, // Node width
        y: props.sourceNode.position.y + 40 + (index * 24) // Align with connection point
      };
    } else {
      // Fallback to default
      sourcePoint = {
        x: props.sourceNode.position.x + 200, // Node width
        y: props.sourceNode.position.y + 40  // Default y position
      };
    }
  } else {
    // For non-condition nodes, use the standard calculation
    sourcePoint = {
      x: props.sourceNode.position.x + 200, // Node width
      y: props.sourceNode.position.y + 40  // Node height / 2
    };
  }

  const targetPoint = {
    x: props.targetNode.position.x,
    y: props.targetNode.position.y + 40 // Node height / 2
  };

  return generateBezierPath(sourcePoint, targetPoint);
});

// 计算标签位置
const labelPosition = computed(() => {
  let sourcePoint;
  
  // For condition nodes, calculate sourcePoint based on the condition
  if (props.sourceNode.type === NodeType.CONDITION && props.sourceNode.conditions) {
    let index = -1;
    if (props.condition.startsWith('case')) {
      index = parseInt(props.condition.slice(4)) - 1;
    } else if (props.condition === 'else' && props.sourceNode.conditions.length > 0) {
      index = props.sourceNode.conditions.length - 1;
    }
    
    if (index >= 0 && index < props.sourceNode.conditions.length) {
      sourcePoint = {
        x: props.sourceNode.position.x + 200, // Node width
        y: props.sourceNode.position.y + 40 + (index * 24) // Align with connection point
      };
    } else {
      // Fallback to default
      sourcePoint = {
        x: props.sourceNode.position.x + 200, // Node width
        y: props.sourceNode.position.y + 40  // Default y position
      };
    }
  } else {
    // For non-condition nodes, use the standard calculation
    sourcePoint = {
      x: props.sourceNode.position.x + 200, // Node width
      y: props.sourceNode.position.y + 40  // Node height / 2
    };
  }

  const targetPoint = {
    x: props.targetNode.position.x,
    y: props.targetNode.position.y + 40 // Node height / 2
  };

  return {
    x: (sourcePoint.x + targetPoint.x) / 2,
    y: (sourcePoint.y + targetPoint.y) / 2
  };
});

// 获取连接颜色
function getConnectionColor(nodeType: NodeType): string {
  switch (nodeType) {
    case NodeType.START:
      return '#4CAF50';
    case NodeType.FUNCTION:
      return '#2196F3';
    case NodeType.CONDITION:
      return '#FF9800';
    case NodeType.END:
      return '#F44336';
    default:
      return '#757575';
  }
}

// 点击连接线
function onConnectionClick() {
  store.selectConnection(props.sourceNode.id, props.condition);
}

// Add new functions for label handling
function getLabelX(condition: string): number {
  const width = getLabelWidth(condition);
  return -width / 2;
}

function getLabelWidth(condition: string): number {
  const label = getDisplayLabel(condition);
  return Math.max(label.length * 8 + 16, 40); // Minimum width of 40px
}

function getDisplayLabel(condition: string): string {
  if (condition.startsWith('case')) {
    return `Case ${condition.slice(4)}`;
  } else if (condition === 'else') {
    return 'ELSE';
  }
  return condition;
}

function getLabelColor(condition: string): string {
  return props.sourceNode.type === NodeType.CONDITION ? '#FF9800' : '#666666';
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
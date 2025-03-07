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
        x="-20"
        y="-10"
        width="40"
        height="20"
        rx="4"
      />
      <text
        text-anchor="middle"
        dominant-baseline="middle"
      >
        {{ condition }}
      </text>
    </g>
  </g>
</template>

<script lang="ts">
import { computed, defineComponent, type PropType } from 'vue';
import type { Node } from '../types/workflow';
import { NodeType } from '../types/workflow';
import { generateBezierPath, calculateConnectionPoint } from '../utils/canvas';
import { useWorkflowStore } from '../stores/workflow';

export default defineComponent({
  name: 'WorkflowConnection',

  props: {
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
  },

  setup(props) {
    const store = useWorkflowStore();

    // 计算连接路径
    const path = computed(() => {
      const sourcePoint = calculateConnectionPoint(
        props.sourceNode.position,
        100, // 节点宽度
        60,  // 节点高度
        false // 输出点
      );

      const targetPoint = calculateConnectionPoint(
        props.targetNode.position,
        100, // 节点宽度
        60,  // 节点高度
        true  // 输入点
      );

      return generateBezierPath(sourcePoint, targetPoint);
    });

    // 计算标签位置
    const labelPosition = computed(() => {
      const sourcePoint = calculateConnectionPoint(
        props.sourceNode.position,
        100,
        60,
        false
      );

      const targetPoint = calculateConnectionPoint(
        props.targetNode.position,
        100,
        60,
        true
      );

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

    // 删除连接
    function deleteConnection() {
      store.deleteConnection(props.sourceNode.id, props.condition);
    }

    return {
      path,
      labelPosition,
      getConnectionColor,
      onConnectionClick,
      deleteConnection
    };
  }
});
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
</style> 
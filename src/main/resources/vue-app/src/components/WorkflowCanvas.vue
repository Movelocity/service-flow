<template>
  <div
    ref="canvasContainer"
    class="workflow-canvas"
    @wheel="onWheel"
    @mousedown="startPan"
    @mousemove="onMouseMove"
    @mouseup="endPan"
    @mouseleave="endPan"
  >
    <svg
      class="workflow-svg"
      :style="{
        transform: `scale(${scale}) translate(${position.x}px, ${position.y}px)`
      }"
    >
      <defs>
        <pattern
          id="grid"
          width="40"
          height="40"
          patternUnits="userSpaceOnUse"
        >
          <path
            d="M 40 0 L 0 0 0 40"
            fill="none"
            stroke="#e0e0e0"
            stroke-width="1"
          />
        </pattern>
      </defs>

      <!-- 网格背景 -->
      <rect
        width="100%"
        height="100%"
        fill="url(#grid)"
      />

      <!-- 连接线 -->
      <template v-for="node in workflow?.nodes" :key="node.id">
        <template v-for="[condition, targetId] in Object.entries(node.nextNodes)" :key="node.id + '-' + targetId">
          <WorkflowConnection
            :source-node="node"
            :target-node="getNode(targetId)"
            :condition="condition"
            :is-selected="isConnectionSelected(node.id, condition)"
          />
        </template>
      </template>

      <!-- 临时连接线 -->
      <path
        v-if="tempConnection.isCreating"
        class="temp-connection"
        :d="tempConnectionPath"
        :style="{
          stroke: tempConnectionColor,
          strokeWidth: '2',
          strokeDasharray: '5,5'
        }"
      />
    </svg>

    <!-- 节点容器 -->
    <div 
      class="nodes-container"
      :style="{
        transform: `scale(${scale}) translate(${position.x}px, ${position.y}px)`
      }"
    >
      <WorkflowNode
        v-for="node in workflow?.nodes"
        :key="node.id"
        :node="node"
        :is-selected="node.id === selectedNodeId"
        @node-move="updateNodePosition"
        @start-connection="startConnection"
        @end-connection="endConnection"
        @node-click="selectNode"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, onMounted, ref } from 'vue';
import type { Node, Position } from '../types/workflow';
import { NodeType } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';
import WorkflowNode from './WorkflowNode.vue';
import WorkflowConnection from './WorkflowConnection.vue';
import { generateBezierPath } from '../utils/canvas';

export default defineComponent({
  name: 'WorkflowCanvas',

  components: {
    WorkflowNode,
    WorkflowConnection
  },

  setup() {
    const store = useWorkflowStore();
    const canvasContainer = ref<HTMLElement | null>(null);

    const workflow = computed(() => store.currentWorkflow);
    const selectedNodeId = computed(() => store.editorState.selectedNodeId);
    const selectedCondition = computed(() => store.editorState.selectedCondition);
    const scale = computed(() => store.editorState.canvasState.scale);
    const position = computed(() => store.editorState.canvasState.position);

    const tempConnection = ref({
      isCreating: false,
      sourceNodeId: '',
      sourcePosition: { x: 0, y: 0 },
      currentPosition: { x: 0, y: 0 }
    });

    // 获取节点
    function getNode(id: string): Node {
      return workflow.value?.nodes.find(n => n.id === id) as Node;
    }

    // 检查连接是否被选中
    function isConnectionSelected(sourceNodeId: string, condition: string): boolean {
      return selectedNodeId.value === sourceNodeId && selectedCondition.value === condition;
    }

    // 获取临时连接线颜色
    const tempConnectionColor = computed(() => {
      const sourceNode = getNode(tempConnection.value.sourceNodeId);
      if (!sourceNode) return '#757575';

      switch (sourceNode.type) {
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
    });

    // 选择节点
    function selectNode(nodeId: string) {
      store.selectNode(nodeId);
    }

    // 更新节点位置
    function updateNodePosition(nodeId: string, position: Position) {
      store.updateNodePosition(nodeId, position);
    }

    // 画布缩放
    function onWheel(event: WheelEvent) {
      event.preventDefault();
      const delta = event.deltaY > 0 ? -0.1 : 0.1;
      const newScale = Math.max(0.5, Math.min(2, scale.value + delta));
      
      store.updateCanvasState({
        scale: newScale
      });
    }

    // 画布平移
    let isPanning = false;
    let lastPosition = { x: 0, y: 0 };

    function startPan(event: MouseEvent) {
      if (event.target === canvasContainer.value) {
        isPanning = true;
        lastPosition = { x: event.clientX, y: event.clientY };
      }
    }

    function onMouseMove(event: MouseEvent) {
      if (isPanning) {
        const dx = (event.clientX - lastPosition.x) / scale.value;
        const dy = (event.clientY - lastPosition.y) / scale.value;
        
        store.updateCanvasState({
          position: {
            x: position.value.x + dx,
            y: position.value.y + dy
          }
        });

        lastPosition = { x: event.clientX, y: event.clientY };
      }

      if (tempConnection.value.isCreating) {
        const rect = canvasContainer.value?.getBoundingClientRect();
        if (rect) {
          tempConnection.value.currentPosition = {
            x: (event.clientX - rect.left) / scale.value - position.value.x,
            y: (event.clientY - rect.top) / scale.value - position.value.y
          };
        }
      }
    }

    function endPan() {
      isPanning = false;
    }

    // 连接线操作
    function startConnection(nodeId: string, isOutput: boolean, event: MouseEvent) {
      if (!isOutput) return;

      const sourceNode = getNode(nodeId);
      if (!sourceNode) return;

      const rect = canvasContainer.value?.getBoundingClientRect();
      if (!rect) return;

      tempConnection.value = {
        isCreating: true,
        sourceNodeId: nodeId,
        sourcePosition: {
          x: sourceNode.position.x + 100, // 节点宽度
          y: sourceNode.position.y + 30   // 节点高度的一半
        },
        currentPosition: {
          x: (event.clientX - rect.left) / scale.value - position.value.x,
          y: (event.clientY - rect.top) / scale.value - position.value.y
        }
      };
    }

    function endConnection(nodeId: string, isOutput: boolean) {
      if (isOutput || !tempConnection.value.isCreating) return;

      if (tempConnection.value.sourceNodeId !== nodeId) {
        const sourceNode = getNode(tempConnection.value.sourceNodeId);
        if (!sourceNode) return;

        // 根据节点类型决定连接条件
        if (sourceNode.type === NodeType.CONDITION) {
          if (!Object.keys(sourceNode.nextNodes).includes('true')) {
            store.addConnection(tempConnection.value.sourceNodeId, nodeId, 'true');
          } else if (!Object.keys(sourceNode.nextNodes).includes('false')) {
            store.addConnection(tempConnection.value.sourceNodeId, nodeId, 'false');
          }
        } else {
          store.addConnection(tempConnection.value.sourceNodeId, nodeId);
        }
      }

      tempConnection.value.isCreating = false;
    }

    // 计算临时连接线路径
    const tempConnectionPath = computed(() => {
      if (!tempConnection.value.isCreating) return '';

      return generateBezierPath(
        tempConnection.value.sourcePosition,
        tempConnection.value.currentPosition
      );
    });

    onMounted(() => {
      if (canvasContainer.value) {
        const rect = canvasContainer.value.getBoundingClientRect();
        store.updateCanvasState({
          position: {
            x: rect.width / 2,
            y: rect.height / 2
          }
        });
      }
    });

    return {
      canvasContainer,
      workflow,
      selectedNodeId,
      scale,
      position,
      tempConnection,
      tempConnectionPath,
      tempConnectionColor,
      getNode,
      isConnectionSelected,
      selectNode,
      updateNodePosition,
      onWheel,
      startPan,
      onMouseMove,
      endPan,
      startConnection,
      endConnection
    };
  }
});
</script>

<style scoped>
.workflow-canvas {
  width: 100%;
  height: 100%;
  background: var(--background-color);
  overflow: hidden;
  position: relative;
}

.workflow-svg {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-origin: 0 0;
  pointer-events: none;
}

.nodes-container {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-origin: 0 0;
  pointer-events: none;
}

.nodes-container :deep(.workflow-node) {
  pointer-events: all;
}

.temp-connection {
  fill: none;
  pointer-events: none;
}
</style> 
<template>
  <div
    ref="canvasContainer"
    class="workflow-canvas"
    @wheel="onWheel"
    @mousedown="startPan"
    @mousemove="onMouseMove"
    @mouseup="endPan"
    @mouseleave="endPan"
    @contextmenu.prevent="showContextMenu"
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
      <NodeConnection
        v-for="conn in connections"
        :key="conn.sourceId + '-' + conn.condition"
        :source-node="getNode(conn.sourceId)"
        :target-node="getNode(conn.targetId)"
        :condition="conn.condition"
        :is-selected="isConnectionSelected(conn.sourceId, conn.condition)"
      />

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
      <NodeElem
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

    <!-- 右键菜单 -->
    <ContextMenu
      :visible="showMenu"
      :position="menuPosition"
      :click-position="clickPosition"
      @close="closeMenu"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import type { Node, Position } from '@/types/workflow';
import { NodeType } from '@/types/workflow';
import { useWorkflowStore } from '@/stores/workflow';
import NodeElem from './NodeElem.vue';
import NodeConnection from './NodeConnection.vue';
import ContextMenu from './ContextMenu.vue';
import { generateBezierPath } from '@/utils/canvas';

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

// 右键菜单状态
const showMenu = ref(false);
const menuPosition = ref({ x: 0, y: 0 });
const clickPosition = ref({ x: 0, y: 0 });

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
  
  // 获取鼠标相对于画布的位置
  const rect = canvasContainer.value?.getBoundingClientRect();
  if (!rect) return;
  
  const mouseX = event.clientX - rect.left;
  const mouseY = event.clientY - rect.top;

  // 计算新的缩放比例
  const delta = event.deltaY > 0 ? 0.9 : 1.1;
  const newScale = Math.max(0.5, Math.min(2, scale.value * delta));

  // 使用 store 的 updateCanvasState 方法，传入鼠标位置
  store.updateCanvasState({
    scale: newScale
  }, { x: mouseX, y: mouseY });
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

// 添加点击外部关闭菜单的处理
function closeMenu() {
  showMenu.value = false;
}

// 显示右键菜单
function showContextMenu(event: MouseEvent) {
  // 获取点击位置相对于画布的坐标
  const rect = canvasContainer.value?.getBoundingClientRect();
  if (!rect) return;

  // 保存实际点击位置（考虑缩放和平移）
  clickPosition.value = {
    x: (event.clientX - rect.left - position.value.x * scale.value) / scale.value,
    y: (event.clientY - rect.top - position.value.y * scale.value) / scale.value
  };

  // 设置菜单显示位置（使用实际屏幕坐标）
  menuPosition.value = {
    x: event.clientX,
    y: event.clientY
  };

  showMenu.value = true;

  // 添加一次性点击事件监听器来关闭菜单
  setTimeout(() => {
    window.addEventListener('click', closeMenu, { once: true });
  }, 0);
}

// 扁平化连接列表
const connections = computed(() => {
  if (!workflow.value?.nodes) return [];
  return workflow.value.nodes.flatMap(node => 
    Object.entries(node.nextNodes).map(([condition, targetId]) => ({
      sourceId: node.id,
      targetId,
      condition
    }))
  );
});

// 在组件卸载时清理事件监听器
onUnmounted(() => {
  window.removeEventListener('click', closeMenu);
});

onMounted(() => {
  if (canvasContainer.value) {
    const rect = canvasContainer.value.getBoundingClientRect();
    store.updateCanvasState({
      position: {
        x: rect.width / 5,
        y: rect.height / 5
      }
    });
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
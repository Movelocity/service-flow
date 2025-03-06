<template>
  <div
    ref="canvasRef"
    class="workflow-canvas"
    @mousedown="onCanvasMouseDown"
    @wheel="onCanvasWheel"
    @contextmenu.prevent="onContextMenu"
  >
    <!-- 网格背景 -->
    <div
      class="canvas-grid"
      :style="{
        transform: `translate(${canvasState.position.x}px, ${canvasState.position.y}px) scale(${canvasState.scale})`,
        backgroundSize: `${gridSize * canvasState.scale}px ${gridSize * canvasState.scale}px`
      }"
    />

    <!-- 节点和连接的容器 -->
    <div
      class="canvas-content"
      :style="{
        transform: `translate(${canvasState.position.x}px, ${canvasState.position.y}px) scale(${canvasState.scale})`,
        transformOrigin: '0 0'
      }"
    >
      <!-- 连接线 -->
      <WorkflowConnection
        v-for="conn in workflow?.connections"
        :key="conn.id"
        :connection="conn"
        :source-node="getNode(conn.sourceNodeId)!"
        :target-node="getNode(conn.targetNodeId)!"
        :is-selected="conn.id === selectedConnectionId"
        :scale="canvasState.scale"
        :canvas-position="canvasState.position"
      />

      <!-- 节点 -->
      <WorkflowNode
        v-for="node in workflow?.nodes"
        :key="node.id"
        :node="node"
        :is-selected="node.id === selectedNodeId"
        @start-connection="startConnection"
        @end-connection="endConnection"
      />

      <!-- 正在创建的连接线 -->
      <svg
        v-if="tempConnection.isCreating"
        class="temp-connection"
        :style="{
          position: 'absolute',
          left: '0',
          top: '0',
          width: '100%',
          height: '100%',
          pointerEvents: 'none',
          overflow: 'visible'
        }"
      >
        <path
          class="connection-line"
          :d="tempConnectionPath"
          :style="{
            stroke: tempConnectionColor,
            strokeWidth: '2',
            fill: 'none'
          }"
        />
      </svg>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.isVisible"
      class="context-menu"
      :style="{
        left: contextMenu.position.x + 'px',
        top: contextMenu.position.y + 'px'
      }"
    >
      <div
        v-for="nodeType in availableNodeTypes"
        :key="nodeType"
        class="context-menu-item"
        @click="addNode(nodeType)"
      >
        添加{{ getNodeTypeName(nodeType) }}节点
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useWorkflowStore } from '../stores/workflow';
import type { Node } from '../types/workflow';
import { NodeType } from '../types/workflow';
import WorkflowNode from './WorkflowNode.vue';
import WorkflowConnection from './WorkflowConnection.vue';
import { generateBezierPath, reverseTransform } from '../utils/canvas';

const store = useWorkflowStore();
const canvasRef = ref<HTMLDivElement>();
const gridSize = 20;

// 从 store 获取状态
const workflow = computed(() => store.currentWorkflow);
const canvasState = computed(() => store.editorState.canvasState);
const selectedNodeId = computed(() => store.editorState.selectedNodeId);
const selectedConnectionId = computed(() => store.editorState.selectedConnectionId);

// 临时连接状态
const tempConnection = ref({
  isCreating: false,
  sourceNodeId: '',
  sourcePosition: { x: 0, y: 0 },
  currentPosition: { x: 0, y: 0 }
});

// 右键菜单状态
const contextMenu = ref({
  isVisible: false,
  position: { x: 0, y: 0 }
});

// 可用的节点类型
const availableNodeTypes = [
  NodeType.START,
  NodeType.FUNCTION,
  NodeType.CONDITION,
  NodeType.END
];

// 获取节点类型名称
function getNodeTypeName(type: NodeType): string {
  switch (type) {
    case NodeType.START:
      return '开始';
    case NodeType.FUNCTION:
      return '功能';
    case NodeType.CONDITION:
      return '条件';
    case NodeType.END:
      return '结束';
  }
}

// 获取节点
function getNode(id: string): Node | undefined {
  return workflow.value?.nodes.find(node => node.id === id);
}

// 获取临时连接线的颜色
const tempConnectionColor = computed(() => {
  const sourceNode = getNode(tempConnection.value.sourceNodeId);
  if (!sourceNode) return '#6c757d';

  switch (sourceNode.type) {
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
});

// 画布事件处理
function onCanvasMouseDown(e: MouseEvent) {
  if (e.button === 0) { // 左键
    // 清除选中状态
    store.setSelectedNode(null);
    store.selectConnection(null);

    // 开始画布拖动
    if (!tempConnection.value.isCreating) {
      store.updateCanvasState({ isDragging: true });
      const startX = e.clientX - canvasState.value.position.x;
      const startY = e.clientY - canvasState.value.position.y;

      function onMouseMove(e: MouseEvent) {
        store.updateCanvasState({
          position: {
            x: e.clientX - startX,
            y: e.clientY - startY
          }
        });
      }

      function onMouseUp() {
        store.updateCanvasState({ isDragging: false });
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
      }

      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
    }
  }
}

// 画布缩放
function onCanvasWheel(e: WheelEvent) {
  e.preventDefault();
  const delta = e.deltaY > 0 ? 0.9 : 1.1;
  const newScale = Math.max(0.1, Math.min(2, canvasState.value.scale * delta));

  // 计算以鼠标位置为中心的缩放
  const rect = canvasRef.value?.getBoundingClientRect();
  if (rect) {
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;

    const oldX = (mouseX - canvasState.value.position.x) / canvasState.value.scale;
    const oldY = (mouseY - canvasState.value.position.y) / canvasState.value.scale;

    const newX = mouseX - oldX * newScale;
    const newY = mouseY - oldY * newScale;

    store.updateCanvasState({
      scale: newScale,
      position: { x: newX, y: newY }
    });
  }
}

// 右键菜单
function onContextMenu(e: MouseEvent) {
  e.preventDefault();
  const rect = canvasRef.value?.getBoundingClientRect();
  if (!rect) return;

  // const position = reverseTransform(
  //   {
  //     x: e.clientX - rect.left,
  //     y: e.clientY - rect.top
  //   },
  //   canvasState.value.scale,
  //   canvasState.value.position
  // );

  contextMenu.value = {
    isVisible: true,
    position: { x: e.clientX, y: e.clientY }
  };

  function hideMenu() {
    contextMenu.value.isVisible = false;
    document.removeEventListener('click', hideMenu);
  }

  document.addEventListener('click', hideMenu);
}

// 添加节点
function addNode(type: NodeType) {
  if (!workflow.value) return;

  const rect = canvasRef.value?.getBoundingClientRect();
  if (!rect) return;

  const position = reverseTransform(
    {
      x: contextMenu.value.position.x - rect.left,
      y: contextMenu.value.position.y - rect.top
    },
    canvasState.value.scale,
    canvasState.value.position
  );

  store.addNode(type, position, getNodeTypeName(type));

  contextMenu.value.isVisible = false;
}

// 连接相关
function startConnection(nodeId: string, isOutput: boolean) {
  if (!isOutput) return;

  const sourceNode = getNode(nodeId);
  if (!sourceNode) return;

  const sourcePos = {
    x: sourceNode.position.x,
    y: sourceNode.position.y
  };

  tempConnection.value = {
    isCreating: true,
    sourceNodeId: nodeId,
    sourcePosition: {
      x: sourcePos.x + 200, // nodeWidth
      y: sourcePos.y + 40   // nodeHeight / 2
    },
    currentPosition: {
      x: sourcePos.x + 200,
      y: sourcePos.y + 40
    }
  };

  function onMouseMove(e: MouseEvent) {
    const rect = canvasRef.value?.getBoundingClientRect();
    if (!rect) return;

    const pos = reverseTransform(
      {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      },
      canvasState.value.scale,
      canvasState.value.position
    );

    tempConnection.value.currentPosition = {
      x: pos.x,
      y: pos.y
    };
  }

  function onMouseUp() {
    tempConnection.value.isCreating = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  }

  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

function endConnection(nodeId: string, isOutput: boolean) {
  if (isOutput || !tempConnection.value.isCreating) return;

  if (tempConnection.value.sourceNodeId !== nodeId) {
    const sourceNode = getNode(tempConnection.value.sourceNodeId);
    if (sourceNode?.type === NodeType.CONDITION) {
      // 对于条件节点，添加 true/false 分支
      if (!store.nodeConnections(tempConnection.value.sourceNodeId).some(conn => conn.condition === 'true')) {
        store.addConnection(tempConnection.value.sourceNodeId, nodeId, 'true');
      } else if (!store.nodeConnections(tempConnection.value.sourceNodeId).some(conn => conn.condition === 'false')) {
        store.addConnection(tempConnection.value.sourceNodeId, nodeId, 'false');
      }
    } else {
      store.addConnection(tempConnection.value.sourceNodeId, nodeId);
    }
  }

  tempConnection.value.isCreating = false;
}

// 临时连接线路径
const tempConnectionPath = computed(() => {
  if (!tempConnection.value.isCreating) return '';

  return generateBezierPath(
    tempConnection.value.sourcePosition,
    tempConnection.value.currentPosition
  );
});
</script>

<style scoped>
.workflow-canvas {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background-color: #f8f9fa;
}

.canvas-grid {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(var(--grid-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  transform-origin: 0 0;
}

.canvas-content {
  position: absolute;
  width: 100%;
  height: 100%;
  transform-origin: 0 0;
}

.context-menu {
  position: absolute;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 4px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
}

.context-menu-item:hover {
  background-color: #f8f9fa;
}

.temp-connection {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

:root {
  --grid-color: rgba(0, 0, 0, 0.1);
}
</style> 
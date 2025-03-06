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
        transform: `translate(${canvasState.position.x}px, ${canvasState.position.y}px) scale(${canvasState.scale})`
      }"
    >
      <!-- 节点 -->
      <WorkflowNode
        v-for="node in workflow?.nodes"
        :key="node.id"
        :node="node"
        :is-selected="node.id === selectedNodeId"
        @start-connection="startConnection"
        @end-connection="endConnection"
      />

      <!-- 连接线 -->
      <WorkflowConnection
        v-for="conn in workflow?.connections"
        :key="conn.id"
        :connection="conn"
        :source-node="getNode(conn.sourceNodeId)!"
        :target-node="getNode(conn.targetNodeId)!"
        :is-selected="false"
      />

      <!-- 正在创建的连接线 -->
      <svg
        v-if="tempConnection.isCreating"
        class="temp-connection"
        :style="{
          position: 'absolute' as const,
          left: '0',
          top: '0',
          width: '100%',
          height: '100%',
          pointerEvents: 'none' as const
        }"
      >
        <path
          class="connection-line"
          :d="tempConnectionPath"
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
        v-for="type in availableNodeTypes"
        :key="type"
        class="context-menu-item"
        @click="addNode(type)"
      >
        添加{{ getNodeTypeName(type) }}节点
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { Position } from '../types/workflow';
import { NodeType } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';
import { generateBezierPath, snapToGrid, reverseTransform } from '../utils/canvas';
import WorkflowNode from './WorkflowNode.vue';
import WorkflowConnection from './WorkflowConnection.vue';

const store = useWorkflowStore();

// 画布引用
const canvasRef = ref<HTMLDivElement | null>(null);

// 工作流数据
const workflow = computed(() => store.currentWorkflow);
const selectedNodeId = computed(() => store.editorState.selectedNodeId);
const canvasState = computed(() => store.editorState.canvasState);

// 网格大小
const gridSize = 20;

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

// 获取节点类型的显示名称
function getNodeTypeName(type: NodeType): string {
  const typeNames: Record<NodeType, string> = {
    [NodeType.START]: '开始',
    [NodeType.FUNCTION]: '函数',
    [NodeType.CONDITION]: '条件',
    [NodeType.END]: '结束'
  };
  return typeNames[type];
}

// 获取节点
function getNode(nodeId: string) {
  return workflow.value?.nodes.find(n => n.id === nodeId);
}

// 画布拖拽
function onCanvasMouseDown(event: MouseEvent) {
  if (event.button !== 0) return; // 只处理左键
  
  const startX = event.clientX - canvasState.value.position.x;
  const startY = event.clientY - canvasState.value.position.y;
  
  function onMouseMove(e: MouseEvent) {
    store.updateCanvasState({
      position: {
        x: e.clientX - startX,
        y: e.clientY - startY
      }
    });
  }
  
  function onMouseUp() {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
  }
  
  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
}

// 画布缩放
function onCanvasWheel(event: WheelEvent) {
  event.preventDefault();
  
  const delta = event.deltaY > 0 ? -0.1 : 0.1;
  const newScale = Math.max(0.5, Math.min(2, canvasState.value.scale + delta));
  
  store.updateCanvasState({
    scale: newScale
  });
}

// 右键菜单
function onContextMenu(event: MouseEvent) {
  const rect = canvasRef.value?.getBoundingClientRect();
  if (!rect) return;
  
  const position = reverseTransform(
    {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    },
    canvasState.value.scale,
    canvasState.value.position
  );
  
  contextMenu.value = {
    isVisible: true,
    position: snapToGrid(position)
  };
  
  function onClickOutside(e: MouseEvent) {
    if (e.target !== event.target) {
      contextMenu.value.isVisible = false;
      document.removeEventListener('click', onClickOutside);
    }
  }
  
  document.addEventListener('click', onClickOutside);
}

// 添加节点
function addNode(type: NodeType) {
  store.addNode(type, contextMenu.value.position);
  contextMenu.value.isVisible = false;
}

// 连接操作
function startConnection(nodeId: string, isOutput: boolean) {
  if (!isOutput) return; // 只允许从输出点开始连接
  
  const sourceNode = getNode(nodeId);
  if (!sourceNode) return;
  
  tempConnection.value = {
    isCreating: true,
    sourceNodeId: nodeId,
    sourcePosition: {
      x: sourceNode.position.x + 200, // nodeWidth
      y: sourceNode.position.y + 40   // nodeHeight / 2
    },
    currentPosition: {
      x: sourceNode.position.x + 200,
      y: sourceNode.position.y + 40
    }
  };
  
  function onMouseMove(e: MouseEvent) {
    const rect = canvasRef.value?.getBoundingClientRect();
    if (!rect) return;
    
    tempConnection.value.currentPosition = reverseTransform(
      {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      },
      canvasState.value.scale,
      canvasState.value.position
    );
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
  if (isOutput || !tempConnection.value.isCreating) return; // 只允许连接到输入点
  
  if (tempConnection.value.sourceNodeId !== nodeId) {
    store.addConnection(tempConnection.value.sourceNodeId, nodeId);
  }
  
  tempConnection.value.isCreating = false;
}

// 计算临时连接线的样式和路径
const tempConnectionStyle = computed(() => ({
  position: 'absolute',
  left: '0',
  top: '0',
  width: '100%',
  height: '100%',
  pointerEvents: 'none'
}));

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
</style> 
<template>
  <!-- 节点编辑器面板 -->
  <div :class="['editor-panel', { visible: isVisible }]">
    <template v-if="selectedNode">
      <div class="editor-header">
        <div class="editor-title-container">
          <NodeIcon v-if="selectedNode" :type="selectedNode.type" :size="24" />
          <input
            v-model="selectedNode.name"
            type="text"
            class="editor-title"
            @change="updateNodeName"
          >
        </div>
        <button class="close-button" @click="onClose">×</button>
      </div>
      
      <input
        v-model="selectedNode.description"
        type="text"
        class="editor-description"
        placeholder="添加描述..."
        @change="updateNodeDescription"
      >
      

      <!-- 开始节点的全局变量定义 -->
      <template v-if="selectedNode.type === 'START'">
        <StartNodeEditor />
      </template>

      <!-- 条件节点特有的配置 -->
      <template v-if="selectedNode.type === 'CONDITION'">
        <ConditionNodeEditor />
      </template>

      <!-- 函数节点特有的配置 -->
      <template v-if="selectedNode.type === 'FUNCTION'">
        <FunctionNodeEditor :nodeId="selectedNode.id" />
      </template>

      <!-- 连接信息 -->
      <div class="outbound-connections" v-if="selectedNode?.type !== 'END'">
        <label class="form-label">下一步</label>
        <div class="connections-list">
          <template v-if="nodeConnections.length">
            <div
              v-for="conn in nodeConnections"
              :key="conn.id"
              class="connection-item"
            >
              <span class="connection-info">
                {{ getConnectionLabel(conn) }}
              </span>
              <el-icon 
                class="icon-btn"
                @click="deleteConnection(conn.id)">
                <Delete />
              </el-icon>
            </div>
          </template>
          <div v-else class="no-connections">
            暂无连接
          </div>
        </div>
      </div>

      <div class="editor-actions" v-if="selectedNode?.type !== 'START'">
        <button
          class="btn btn-delete"
          @click="deleteNode"
        >
          删除节点
        </button>
      </div>
    </template>
  </div>

</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import { Delete } from '@element-plus/icons-vue';
import NodeIcon from '@/components/common/NodeIcon.vue';
import StartNodeEditor from '@/components/node-editors/StartNodeEditor.vue';
import ConditionNodeEditor from '@/components/node-editors/ConditionNodeEditor.vue';
import FunctionNodeEditor from '@/components/node-editors/FunctionNodeEditor.vue';

defineProps<{
  isVisible: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const store = useWorkflowStore();
const selectedNode = computed(() => store.selectedNode);

// 获取节点的连接
const nodeConnections = computed(() => {
  if (!selectedNode.value) return [];
  const outputs = store.nodeOutputConnections(selectedNode.value.id);
  
  return [
    ...outputs.map(conn => ({
      id: `${selectedNode.value!.id}-${conn.condition}`,
      sourceNodeId: selectedNode.value!.id,
      targetNodeId: conn.targetId,
      condition: conn.condition
    }))
  ];
});

// 更新节点名称
function updateNodeName() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, selectedNode.value);
  }
}

// 更新节点描述
function updateNodeDescription() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, selectedNode.value);
  }
}

// 删除连接
function deleteConnection(connectionId: string) {
  const connection = nodeConnections.value.find(conn => conn.id === connectionId);
  if (connection) {
    store.deleteConnection(connection.sourceNodeId, connection.condition);
  }
}

// 删除节点
function deleteNode() {
  if (selectedNode.value) {
    store.deleteNode(selectedNode.value.id);
    onClose();
  }
}

// 获取连接的描述文本
function getConnectionLabel(connection: { sourceNodeId: string; targetNodeId: string; condition?: string }) {
  // const isSource = connection.sourceNodeId === selectedNode.value?.id;
  const otherNodeId = connection.sourceNodeId === selectedNode.value?.id ? connection.targetNodeId : connection.sourceNodeId;
  const otherNode = store.currentWorkflow?.nodes.find(n => n.id === otherNodeId);
  
  return `${otherNode?.name || otherNodeId}${connection.condition ? ` (条件: ${connection.condition})` : ''}`;
}

// 关闭面板
function onClose() {
  store.selectNode(null);
  emit('close');
}

</script>

<style scoped>
.editor-panel {
  position: fixed;
  top: 70px;
  right: 0;
  width: 400px;
  border-radius: 1rem 0 0 1rem;
  height: calc(100vh - 70px);
  background: var(--card-bg);
  box-shadow: 0 0 2px var(--card-shadow);
  border-left: 1px solid var(--border-color);
  padding: 20px;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  z-index: 1000;
  overflow-y: auto;
  color: var(--text-color);
}

.editor-panel.visible {
  transform: translateX(0);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  /* border-bottom: 1px solid var(--border-color); */
}

.editor-title-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.editor-title {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-color);
  border: none;
  background: none;
  max-width: 200px;
  outline: none;
}

.editor-description {
  margin: 0;
  font-size: 1rem;
  color: var(--el-text-color-secondary);
  width: 100%;
  border: none;
  background: none;
  outline: none;
  border-bottom: 1px solid var(--border-color);
  padding: 10px;
}

.editor-description::placeholder {
  color: var(--el-text-color-secondary);
}

.close-button {
  background: none;
  border: none;
  font-size: 2rem;
  line-height: 1;
  padding: 0;
  cursor: pointer;
  color: var(--text-color);
  opacity: 0.7;
}

.close-button:hover {
  opacity: 1;
}

.form-text {
  font-size: 0.875rem;
  margin-top: 0.25rem;
  color: var(--text-color);
  opacity: 0.7;
}

.form-control {
  background-color: var(--node-bg);
  border-color: var(--border-color);
  color: var(--text-color);
}

.form-control:focus {
  background-color: var(--node-bg);
  border-color: var(--node-selected);
  color: var(--text-color);
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.form-label {
  color: var(--text-color);
}

.connections-list {
  margin-top: 0.5rem;
}

.connection-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  margin-bottom: 8px;
  background: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.connection-info {
  color: var(--text-color);
}

.no-connections {
  padding: 8px;
  color: var(--text-color);
  opacity: 0.7;
  text-align: center;
  border: 1px dashed var(--border-color);
  border-radius: 4px;
}

.editor-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.variables-list {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.variable-item {
  padding: 1rem;
  margin-bottom: 1rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.variable-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 0.5rem;
  align-items: center;
}

.variable-row .form-control {
  flex: 1;
}

.type-select {
  width: 120px;
}

.variable-options {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.variable-options .form-control {
  width: 150px;
}

.btn-outline-primary {
  color: var(--text-color);
  border-color: var(--border-color);
  background: transparent;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-outline-primary:hover {
  color: var(--text-color);
  background: var(--node-bg);
  border-color: var(--node-selected);
}

.mt-2 {
  margin-top: 0.5rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.w-100 {
  width: 100%;
}

.variable-form {
  padding: 1rem;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

/* 添加对话框相关样式 */
:deep(.variable-dialog) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-dialog) {
  margin: 0 auto;
  max-width: 90%;
}

:deep(.el-select) {
  width: 100%;
}

.outbound-connections {
  border-top: 1px solid var(--border-color);
  margin-top: 1rem;
  padding-top: 1rem;
}
</style> 
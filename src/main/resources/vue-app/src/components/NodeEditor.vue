<template>
  <div :class="['editor-panel', { visible: isVisible }]">
    <div class="editor-header">
      <h3 class="editor-title">{{ title }}</h3>
      <button class="close-button" @click="onClose">×</button>
    </div>

    <template v-if="selectedNode && nodeData">
      <div class="form-group">
        <label class="form-label" for="nodeName">节点名称</label>
        <input
          id="nodeName"
          v-model="nodeData.name"
          type="text"
          class="form-control"
          @change="updateNode"
        >
      </div>

      <!-- 条件节点特有的配置 -->
      <template v-if="selectedNode.type === 'CONDITION'">
        <div class="form-group">
          <label class="form-label" for="condition">条件表达式</label>
          <textarea
            id="condition"
            v-model="nodeData.parameters.condition"
            class="form-control"
            rows="3"
            placeholder="请输入条件表达式"
            @change="updateNode"
          />
          <small class="form-text text-muted">
            支持JavaScript表达式，例如：value > 100
          </small>
        </div>
      </template>

      <!-- 函数节点特有的配置 -->
      <template v-if="selectedNode.type === 'FUNCTION'">
        <div class="form-group">
          <label class="form-label" for="function">函数名称</label>
          <input
            id="function"
            v-model="nodeData.parameters.function"
            type="text"
            class="form-control"
            placeholder="请输入函数名称"
            @change="updateNode"
          >
        </div>
        
        <div class="form-group">
          <label class="form-label" for="parameters">函数参数</label>
          <textarea
            id="parameters"
            v-model="parametersJson"
            class="form-control"
            rows="5"
            placeholder="请输入JSON格式的参数"
            @change="updateParameters"
          />
          <small class="form-text text-muted">
            使用JSON格式定义参数，例如：{"param1": "value1"}
          </small>
        </div>
      </template>

      <!-- 连接信息 -->
      <div class="form-group">
        <label class="form-label">连接</label>
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
              <button
                class="btn btn-sm btn-danger"
                @click="deleteConnection(conn.id)"
              >
                删除
              </button>
            </div>
          </template>
          <div v-else class="no-connections">
            暂无连接
          </div>
        </div>
      </div>

      <div class="editor-actions">
        <button
          class="btn btn-danger"
          @click="deleteNode"
        >
          删除节点
        </button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { Node } from '../types/workflow';
import { useWorkflowStore } from '../stores/workflow';

defineProps<{
  isVisible: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();

const store = useWorkflowStore();

const selectedNode = computed(() => store.selectedNode);

const title = computed(() => {
  if (!selectedNode.value) return '节点编辑';
  return `编辑${selectedNode.value.type}节点`;
});

// 节点数据的本地副本
const nodeData = ref<Node | null>(null);

// 监听选中节点的变化
watch(selectedNode, (newNode) => {
  if (newNode) {
    nodeData.value = JSON.parse(JSON.stringify(newNode));
  } else {
    nodeData.value = null;
  }
}, { immediate: true });

// 函数参数的JSON字符串表示
const parametersJson = computed({
  get() {
    if (!nodeData.value?.parameters) return '';
    const params = { ...nodeData.value.parameters };
    delete params.function; // 排除function字段
    return JSON.stringify(params, null, 2);
  },
  set(value: string) {
    try {
      const params = JSON.parse(value);
      if (nodeData.value) {
        nodeData.value.parameters = {
          ...params,
          function: nodeData.value.parameters.function
        };
      }
    } catch (e) {
      console.error('Invalid JSON:', e);
    }
  }
});

// 获取节点的连接
const nodeConnections = computed(() => {
  if (!selectedNode.value) return [];
  const inputs = store.nodeInputConnections(selectedNode.value.id);
  const outputs = store.nodeOutputConnections(selectedNode.value.id);
  
  return [
    ...inputs.map(conn => ({
      id: `${conn.sourceId}-${conn.condition}`,
      sourceNodeId: conn.sourceId,
      targetNodeId: selectedNode.value!.id,
      condition: conn.condition
    })),
    ...outputs.map(conn => ({
      id: `${selectedNode.value!.id}-${conn.condition}`,
      sourceNodeId: selectedNode.value!.id,
      targetNodeId: conn.targetId,
      condition: conn.condition
    }))
  ];
});

// 更新节点
function updateNode() {
  if (nodeData.value && selectedNode.value) {
    store.updateNode(selectedNode.value.id, nodeData.value);
  }
}

// 更新参数
function updateParameters() {
  updateNode();
}

// 删除连接
function deleteConnection(connectionId: string) {
  const [sourceId, condition] = connectionId.split('-');
  if (sourceId === selectedNode.value?.id) {
    // 如果当前节点是源节点，直接删除其nextNodes中的连接
    store.deleteConnection(sourceId, condition);
  } else {
    // 如果当前节点是目标节点，找到源节点并删除连接
    store.deleteConnection(sourceId, condition);
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
  const isSource = connection.sourceNodeId === selectedNode.value?.id;
  const otherNodeId = isSource ? connection.targetNodeId : connection.sourceNodeId;
  const otherNode = store.currentWorkflow?.nodes.find(n => n.id === otherNodeId);
  
  return `${isSource ? '输出到' : '输入自'} ${otherNode?.name || otherNodeId}${
    connection.condition ? ` (条件: ${connection.condition})` : ''
  }`;
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
  top: 60px;
  right: 0;
  width: 300px;
  height: calc(100vh - 60px);
  background: var(--card-bg);
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
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
}

.editor-title {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-color);
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
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
</style> 
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import workflowApi from '../api/workflow'

// 使用useRoute获取当前路由信息
const route = useRoute()
const router = useRouter()

// 从路由参数中获取ID和isNew状态
const workflowId = computed(() => route.params.id as string)
const isNewWorkflow = computed(() => route.name === 'NewWorkflow')

const workflow = ref<any>({
  id: '',
  name: '',
  description: '',
  globalVariables: {},
  nodes: [],
  startNodeId: '',
  active: true
})

const loading = ref(true)
const saving = ref(false)
const error = ref<string | null>(null)
const selectedNode = ref<any>(null)
const showNodeEditor = ref(false)

// Load workflow data on component mount
onMounted(async () => {
  if (isNewWorkflow.value) {
    // Initialize a new workflow
    workflow.value = {
      id: `workflow_${Date.now()}`,
      name: '新工作流',
      description: '',
      globalVariables: {},
      nodes: [
        {
          id: 'node_1',
          name: '开始',
          type: 'START',
          parameters: {},
          nextNodes: { default: '' },
          position: { x: 100, y: 100 }
        }
      ],
      startNodeId: 'node_1',
      active: true
    }
    loading.value = false
  } else {
    try {
      loading.value = true
      console.log('正在获取工作流，ID:', workflowId.value)
      
      if (!workflowId.value || workflowId.value === 'undefined') {
        throw new Error('无效的工作流ID')
      }
      
      const data = await workflowApi.fetchWorkflow(workflowId.value)
      console.log('获取到的工作流数据:', data)
      workflow.value = data
    } catch (err) {
      console.error('获取工作流失败:', err)
      error.value = '加载工作流失败，请重试。' + (err instanceof Error ? err.message : '')
    } finally {
      loading.value = false
    }
  }
})

// Save workflow
const saveWorkflow = async () => {
  if (!workflow.value.name.trim()) {
    alert('工作流名称不能为空')
    return
  }

  try {
    saving.value = true
    console.log('保存工作流:', workflow.value)
    await workflowApi.saveWorkflow(workflow.value)
    alert('工作流保存成功')
    if (isNewWorkflow.value) {
      router.push('/')
    }
  } catch (err) {
    console.error('保存工作流失败:', err)
    alert('保存工作流失败')
  } finally {
    saving.value = false
  }
}

// Delete workflow
const deleteWorkflow = async () => {
  if (!workflow.value.id) return
  if (!confirm('确定要删除这个工作流吗？')) return

  try {
    await workflowApi.deleteWorkflow(workflow.value.id)
    alert('工作流删除成功')
    router.push('/')
  } catch (err) {
    console.error('删除工作流失败:', err)
    alert('删除工作流失败')
  }
}

// Test workflow
const testWorkflow = async () => {
  if (!workflow.value.id) return
  try {
    const input = {} // 可以添加输入参数
    const executionId = await workflowApi.startWorkflowExecution(workflow.value.id, input)
    alert(`工作流开始执行，执行ID: ${executionId}`)
  } catch (err) {
    console.error('执行工作流失败:', err)
    alert('执行工作流失败')
  }
}

// Add node to workflow
const addNode = (type: string, x: number, y: number) => {
  const nodeId = `node_${Date.now()}`
  let nodeName = '';
  
  switch(type) {
    case 'START':
      nodeName = '开始';
      break;
    case 'CONDITION':
      nodeName = '条件';
      break;
    case 'FUNCTION':
      nodeName = '功能';
      break;
    case 'END':
      nodeName = '结束';
      break;
    default:
      nodeName = type;
  }
  
  const newNode = {
    id: nodeId,
    name: nodeName,
    type,
    parameters: {},
    nextNodes: type === 'END' ? {} : { default: '' },
    position: { x, y }
  }
  
  workflow.value.nodes.push(newNode)
  return newNode
}

// Remove node from workflow
const removeNode = (nodeId: string) => {
  if (nodeId === workflow.value.startNodeId) {
    alert('不能删除开始节点')
    return
  }
  
  workflow.value.nodes = workflow.value.nodes.filter((node: any) => node.id !== nodeId)
  workflow.value.nodes.forEach((node: any) => {
    if (node.nextNodes) {
      Object.keys(node.nextNodes).forEach(key => {
        if (node.nextNodes[key] === nodeId) {
          node.nextNodes[key] = ''
        }
      })
    }
  })
}

// Edit node
const editNode = (node: any) => {
  selectedNode.value = { ...node }
  showNodeEditor.value = true
}

// Save node changes
const saveNodeChanges = () => {
  if (!selectedNode.value) return
  
  const index = workflow.value.nodes.findIndex((n: any) => n.id === selectedNode.value.id)
  if (index !== -1) {
    workflow.value.nodes[index] = { ...selectedNode.value }
  }
  
  showNodeEditor.value = false
  selectedNode.value = null
}

// Handle context menu
const handleContextMenu = (event: MouseEvent) => {
  event.preventDefault()
  const contextMenu = document.getElementById('contextMenu')
  if (contextMenu) {
    contextMenu.style.display = 'block'
    contextMenu.style.left = `${event.clientX}px`
    contextMenu.style.top = `${event.clientY}px`
  }
}

// Close context menu
const closeContextMenu = () => {
  const contextMenu = document.getElementById('contextMenu')
  if (contextMenu) {
    contextMenu.style.display = 'none'
  }
}

// Handle canvas click
const handleCanvasClick = (event: MouseEvent) => {
  closeContextMenu()
}
</script>

<template>
  <div class="workflow-editor-container">
    <!-- Editor Header -->
    <div class="editor-header">
      <div class="editor-header-left">
        <a href="/" class="back-link">
          <span class="icon-back"></span>
          返回列表
        </a>
        <input 
          type="text" 
          v-model="workflow.name"
          class="workflow-name-input"
          placeholder="工作流名称"
        >
        <input 
          type="text" 
          v-model="workflow.description"
          class="workflow-desc-input"
          placeholder="工作流描述"
        >
      </div>
      <div class="editor-header-right">
        <button class="button success" @click="saveWorkflow" :disabled="saving">
          <span class="icon-save"></span>{{ saving ? '保存中...' : '保存' }}
        </button>
        <button class="button primary" @click="testWorkflow">
          <span class="icon-play"></span>运行
        </button>
        <button class="button danger" @click="deleteWorkflow">
          <span class="icon-delete"></span>删除
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>

    <!-- Workflow Canvas -->
    <div v-else class="workflow-canvas" @contextmenu="handleContextMenu" @click="handleCanvasClick">
      <div 
        v-for="node in workflow.nodes" 
        :key="node.id"
        class="node"
        :class="'node-' + node.type.toLowerCase()"
        :style="{
          left: node.position.x + 'px',
          top: node.position.y + 'px'
        }"
        @dblclick="editNode(node)"
      >
        <div class="node-header">{{ node.name }}</div>
        <div class="node-type">{{ node.type }}</div>
        <div class="connection-point input"></div>
        <div class="connection-point output"></div>
      </div>
    </div>

    <!-- Context Menu -->
    <div class="context-menu" id="contextMenu">
      <div class="context-menu-item" @click="addNode('START', 100, 100)">
        <span class="icon-start"></span>开始节点
      </div>
      <div class="context-menu-item" @click="addNode('FUNCTION', 200, 200)">
        <span class="icon-function"></span>功能节点
      </div>
      <div class="context-menu-item" @click="addNode('CONDITION', 200, 200)">
        <span class="icon-condition"></span>条件节点
      </div>
      <div class="context-menu-item" @click="addNode('END', 300, 100)">
        <span class="icon-end"></span>结束节点
      </div>
    </div>

    <!-- Node Editor Panel -->
    <div class="node-editor-panel" :class="{ visible: showNodeEditor }">
      <div class="node-editor-header">
        <h3>编辑节点</h3>
        <span class="node-editor-close" @click="showNodeEditor = false">
          <span class="icon-close"></span>
        </span>
      </div>
      <div class="node-editor-body" v-if="selectedNode">
        <div class="form-group">
          <label>节点名称</label>
          <input type="text" v-model="selectedNode.name">
        </div>
        <div class="form-group">
          <label>参数</label>
          <textarea 
            v-model="selectedNode.parameters"
            rows="4"
            placeholder="输入参数，格式为JSON"
          ></textarea>
        </div>
        <div class="form-group" v-if="selectedNode.type !== 'END'">
          <label>下一节点</label>
          <div v-for="(targetId, condition) in selectedNode.nextNodes" :key="condition">
            <div class="input-group">
              <span class="input-label">{{ condition }}</span>
              <select v-model="selectedNode.nextNodes[condition]">
                <option value="">-- 选择节点 --</option>
                <option 
                  v-for="node in workflow.nodes" 
                  :key="node.id"
                  :value="node.id"
                  v-if="node.id !== selectedNode.id"
                >
                  {{ node.name }} ({{ node.id }})
                </option>
              </select>
            </div>
          </div>
        </div>
        <button class="button primary w-full" @click="saveNodeChanges">
          保存更改
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.workflow-editor-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-header {
  padding: 10px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.editor-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.editor-header-right {
  display: flex;
  gap: 8px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #374151;
}

.workflow-name-input,
.workflow-desc-input {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

.workflow-name-input {
  width: 200px;
}

.workflow-desc-input {
  width: 300px;
}

.workflow-canvas {
  flex: 1;
  background-color: #f8f9fa;
  position: relative;
  overflow: auto;
  height: calc(100vh - 60px);
}

.node {
  border: 2px solid #6c757d;
  border-radius: 6px;
  padding: 10px;
  background-color: white;
  width: 200px;
  position: absolute;
  cursor: move;
  user-select: none;
}

.node-start { border-color: #10b981; }
.node-end { border-color: #ef4444; }
.node-condition { border-color: #f59e0b; }
.node-function { border-color: #3b82f6; }

.node-header {
  font-weight: bold;
  margin-bottom: 5px;
}

.node-type {
  font-size: 0.8em;
  color: #6c757d;
}

.connection-point {
  width: 10px;
  height: 10px;
  background: #6c757d;
  border-radius: 50%;
  position: absolute;
  cursor: pointer;
}

.connection-point.input {
  top: 50%;
  left: -5px;
  transform: translateY(-50%);
}

.connection-point.output {
  top: 50%;
  right: -5px;
  transform: translateY(-50%);
}

.context-menu {
  position: fixed;
  z-index: 1000;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: none;
}

.context-menu-item {
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
}

.context-menu-item:hover {
  background-color: #f3f4f6;
}

.node-editor-panel {
  position: fixed;
  top: 60px;
  right: 0;
  width: 300px;
  height: calc(100vh - 60px);
  background-color: white;
  border-left: 1px solid #e5e7eb;
  padding: 16px;
  transform: translateX(100%);
  transition: transform 0.3s ease;
  z-index: 900;
}

.node-editor-panel.visible {
  transform: translateX(0);
}

.node-editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
}

.node-editor-close {
  cursor: pointer;
  padding: 4px;
}

.node-editor-body {
  padding: 16px 0;
}

.button {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  border: 1px solid transparent;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.button.success {
  background-color: #10b981;
  color: white;
}

.button.success:hover {
  background-color: #059669;
}

.button.primary {
  background-color: #3b82f6;
  color: white;
}

.button.primary:hover {
  background-color: #2563eb;
}

.button.danger {
  background-color: #ef4444;
  color: white;
}

.button.danger:hover {
  background-color: #dc2626;
}

.button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.w-full {
  width: 100%;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
}

.input-group {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.input-label {
  background-color: #f3f4f6;
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-right: none;
  border-radius: 4px 0 0 4px;
  font-size: 14px;
  min-width: 80px;
}

.input-group select {
  flex: 1;
  border-radius: 0 4px 4px 0;
}

/* Icons */
.icon-back::before { content: "←"; }
.icon-save::before { content: "💾"; }
.icon-play::before { content: "▶"; }
.icon-delete::before { content: "🗑"; }
.icon-close::before { content: "×"; }
.icon-start::before { content: "▶"; }
.icon-end::before { content: "⬛"; }
.icon-function::before { content: "⚙"; }
.icon-condition::before { content: "◆"; }

/* Loading and Error States */
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top-color: #3b82f6;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  padding: 16px;
  margin: 20px;
  background-color: #fee2e2;
  color: #b91c1c;
  border-radius: 4px;
}
</style> 
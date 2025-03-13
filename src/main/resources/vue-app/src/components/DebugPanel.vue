<template>
  <div class="debug-panel" :class="{ 'collapsed': isCollapsed }">
    <div class="debug-panel-header">
      <div class="header-left">
        <button class="collapse-btn" @click="toggleCollapse">
          <span class="collapse-icon" :class="{ 'collapsed': isCollapsed }">
            {{ isCollapsed ? '>' : '<' }}
          </span>
        </button>
        <h5>调试信息</h5>
      </div>
      <button class="btn btn-sm btn-danger" @click="$emit('stop')">停止调试</button>
    </div>
    <div class="debug-panel-content">
      <div v-if="showInputForm" class="input-form">
        <h6>请输入工作流参数：</h6>
        <div v-for="(_value, key) in requiredInputs" :key="key" class="form-group">
          <label :for="key">{{ key }}</label>
          <input
            :id="key"
            v-model="inputValues[key]"
            class="form-control form-control-sm"
            :placeholder="'请输入' + key"
          />
        </div>
        <div class="form-actions">
          <button class="btn btn-sm btn-primary" @click="startDebug">开始调试</button>
        </div>
      </div>
      <div v-else>
        <div v-for="(event, index) in events" :key="index" class="debug-event">
          <div class="event-header">
            <div class="row">
              <NodeIcon v-if="event.nodeType" :type="event.nodeType" :size="24" />
              <span class="node-name">{{ event.nodeName }}</span>
            </div>
            <span class="event-type" :class="event.eventType.toLowerCase()">
              {{ event.eventType === 'ENTER' ? '进入' : '完成' }}
            </span>
            <span class="event-time">{{ new Date(event.timestamp).toLocaleTimeString() }}</span>
          </div>
          <div v-if="event.nodeContext" class="event-context">
            <strong>节点上下文：</strong>
            <pre>{{ JSON.stringify(event.nodeContext, null, 2) }}</pre>
          </div>
          <div v-if="event.globalVariables" class="event-variables">
            <strong>全局变量：</strong>
            <pre>{{ JSON.stringify(event.globalVariables, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import NodeIcon from '@/components/NodeIcon.vue';

const props = defineProps<{
  events: any[],
  workflowId: string,
  requiredInputs?: Record<string, any>
}>();

const emit = defineEmits<{
  (e: 'stop'): void,
  (e: 'debug-start', inputs: Record<string, any>): void
}>();

const isCollapsed = ref(false);
const showInputForm = ref(true);
const inputValues = ref<Record<string, any>>({});

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

const startDebug = () => {
  showInputForm.value = false;
  emit('debug-start', inputValues.value);
};

onMounted(() => {
  // 初始化输入值
  if (props.requiredInputs) {
    Object.keys(props.requiredInputs).forEach(key => {
      inputValues.value[key] = '';
    });
  }
});
</script>

<style scoped>
.debug-panel {
  position: fixed;
  right: 0;
  top: 60px;
  bottom: 0;
  width: 400px;
  background: var(--card-bg);
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: transform 0.3s ease;
}

.debug-panel.collapsed {
  transform: translateX(calc(100% - 40px));
}

.debug-panel-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--card-bg);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.collapse-btn {
  background: none;
  border: none;
  padding: 4px 8px;
  cursor: pointer;
  color: var(--text-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.collapse-icon {
  font-size: 1.2em;
  transition: transform 0.3s ease;
}

.collapse-icon.collapsed {
  transform: rotate(180deg);
}

.debug-panel-header h5 {
  margin: 0;
  font-size: 1.2rem;
  color: var(--text-color);
}

.debug-panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.input-form {
  background: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.input-form h6 {
  margin-bottom: 1rem;
  color: var(--text-color);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.form-control {
  width: 100%;
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  line-height: 1.5;
  color: var(--text-color);
  background-color: var(--background-color);
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
}

.form-actions {
  margin-top: 1rem;
  text-align: right;
}

.debug-event {
  background: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 0.25rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.event-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0.5rem;
}

.node-name {
  font-weight: bold;
  color: var(--text-color);
  margin-left: 10px;
}

.event-type {
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.event-type.enter {
  background: var(--primary-color);
  color: white;
}

.event-type.complete {
  background: var(--success-color);
  color: white;
}

.event-time {
  margin-left: auto;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.event-context,
.event-variables {
  margin-top: 0.5rem;
}

.event-context strong,
.event-variables strong {
  display: block;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.event-context pre,
.event-variables pre {
  margin: 0;
  padding: 0.5rem;
  background: var(--background-color);
  border-radius: 0.25rem;
  font-size: 0.875rem;
  overflow-x: auto;
  color: var(--text-color);
}
</style> 
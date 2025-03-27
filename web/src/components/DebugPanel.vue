<template>
  <div class="debug-panel" :class="{ 'collapsed': isCollapsed }">
    <div class="debug-panel-header">
      <div class="header-left">
        <button class="collapse-btn" @click="toggleCollapse">
          <span class="collapse-icon" :class="{ 'collapsed': isCollapsed }"> &gt; </span>
        </button>
        <h5>调试信息</h5>
        <div class="text-btn" @click="resetDebug">reset</div>
      </div>
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
        <div v-for="(node, index) in groupedEvents" :key="index" class="debug-event">
          <div class="event-header">
            <div class="row">
              <NodeIcon v-if="node.enterEvent.nodeType" :type="node.enterEvent.nodeType" :size="24" />
              <span class="node-name">{{ node.enterEvent.nodeName }}</span>
            </div>
            <span v-if="node.completeEvent" class="event-status complete">
              完成
              <span class="event-duration" v-if="node.duration">({{ node.duration }}ms)</span>
            </span>
            <span v-else class="event-status running">
              运行中...
            </span>
            <span class="event-time">{{ new Date(node.enterEvent.timestamp).toLocaleTimeString() }}</span>
          </div>
          <div v-if="node.enterEvent.contextVariables" class="event-context">
            <strong>输入: </strong>
            <pre>{{ JSON.stringify(node.enterEvent.contextVariables, null, 2) }}</pre>
          </div>
          <div v-if="node.completeEvent && node.completeEvent.nodeResult" class="event-variables">
            <strong>输出: </strong>
            <pre>{{ JSON.stringify(node.completeEvent.nodeResult, null, 2) }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useDebugStore } from '@/stores/debug';
import NodeIcon from '@/components/common/NodeIcon.vue';
import type { NodeExecutionEvent } from '@/types/debug';

// Use the debug store instead of props
const debugStore = useDebugStore();

// Interface for grouped events
interface GroupedEvent {
  enterEvent: NodeExecutionEvent;
  completeEvent?: NodeExecutionEvent;
  duration?: number;
}

// Compute properties from the store
const events = computed(() => debugStore.debugEvents);
const requiredInputs = computed(() => debugStore.workflowInputs);
const showInputForm = computed(() => debugStore.showInputForm);

// Group ENTER and COMPLETE events with the same nodeId
const groupedEvents = computed<GroupedEvent[]>(() => {
  const result: GroupedEvent[] = [];
  const pendingNodes = new Map<string, GroupedEvent>();
  
  for (const event of events.value) {
    if (event.eventType === 'ENTER') {
      // Create a new node execution group
      pendingNodes.set(event.nodeId, {
        enterEvent: event,
      });
      result.push(pendingNodes.get(event.nodeId)!);
    } else if (event.eventType === 'COMPLETE') {
      // Look for the matching ENTER event
      const pendingNode = pendingNodes.get(event.nodeId);
      
      if (pendingNode) {
        // Calculate duration if timestamps are available
        let duration: number | undefined = undefined;
        if (pendingNode.enterEvent.timestamp && event.timestamp) {
          const enterTime = new Date(pendingNode.enterEvent.timestamp).getTime();
          const completeTime = new Date(event.timestamp).getTime();
          duration = completeTime - enterTime;
        }
        
        // Update the node with COMPLETE info
        pendingNode.completeEvent = event;
        pendingNode.duration = duration;
        
        // Remove from pending map as it's now complete
        pendingNodes.delete(event.nodeId);
      } else {
        // If we somehow get a COMPLETE without an ENTER, create a standalone entry
        result.push({
          enterEvent: event,
          completeEvent: event
        });
      }
    }
  }
  
  return result;
});

const isCollapsed = ref(false);
const inputValues = ref<Record<string, any>>({});

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value;
};

const startDebug = async () => {
  try {
    debugStore.showInputForm = false;
    await debugStore.startDebug(inputValues.value);
  } catch (error) {
    console.error('Debug start error:', error);
    alert(error instanceof Error ? error.message : '启动调试失败');
  }
};

const resetDebug = () => {
  debugStore.resetDebugEvents();
  // 重置本地输入值
  if (requiredInputs.value) {
    Object.keys(requiredInputs.value).forEach(key => {
      inputValues.value[key] = '';
    });
  }
};

onMounted(() => {
  // 初始化输入值
  if (requiredInputs.value) {
    Object.keys(requiredInputs.value).forEach(key => {
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
  padding: 0.25rem;
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

.event-status {
  padding: 0rem 0.25rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
}

.event-status.running {
  color: var(--el-color-primary);
}

.event-status.complete {
  color: var(--el-color-success);
}

.event-duration {
  font-size: 0.75rem;
  color: var(--text-secondary);
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

.text-btn {
  cursor: pointer;
  color: var(--el-color-primary);
  font-size: 0.875rem;
}

.text-btn:hover {
  text-decoration: underline;
}
</style> 
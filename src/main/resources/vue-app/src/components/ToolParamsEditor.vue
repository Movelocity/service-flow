<template>
  <div class="tool-params-editor">
    <!-- 工具选择 -->
    <div class="form-group">
      <label class="form-label">工具选择</label>
      <el-select
        v-model="selectedTool"
        class="tool-select"
        placeholder="选择工具"
        @change="onToolChange"
      >
        <el-option
          v-for="tool in availableTools"
          :key="tool.name"
          :label="tool.name"
          :value="tool.name"
        >
          <div class="tool-option">
            <span class="tool-name">{{ tool.name }}</span>
            <small class="tool-description">{{ tool.description }}</small>
          </div>
        </el-option>
      </el-select>
    </div>

    <!-- 工具参数配置 -->
    <template v-if="toolDefinition">
      <div class="form-group">
        <label class="form-label">输入参数</label>
        <div class="params-list">
          <div
            v-for="(field, key) in toolDefinition.inputFields"
            :key="key"
            class="param-item"
          >
            <div class="param-header">
              <label class="param-label">
                {{ field.name }}
                <span v-if="field.required" class="required">*</span>
              </label>
              <small class="param-type">{{ field.type }}</small>
            </div>
            
            <div class="param-content">
              <small class="param-description">{{ field.description }}</small>
              
              <!-- 字符串类型参数 -->
              <template v-if="field.type === 'STRING'">
                <div class="param-input-row">
                  <!-- 变量引用选择器 -->
                  <el-select
                    v-model="paramSource[key]"
                    class="param-source-select"
                    placeholder="参数来源"
                  >
                    <el-option label="固定值" value="static" />
                    <el-option label="变量引用" value="variable" />
                  </el-select>
                  
                  <!-- 根据来源显示不同的输入方式 -->
                  <template v-if="paramSource[key] === 'variable'">
                    <VariableSelector
                      v-model="params[key]"
                      :required-type="field.type"
                      placeholder="选择变量"
                      @change="updateParams"
                    />
                  </template>
                  <template v-else>
                    <input
                      v-model="params[key]"
                      type="text"
                      class="form-control"
                      :placeholder="field.defaultValue || ''"
                      @input="updateParams"
                    >
                  </template>
                </div>
              </template>

              <!-- 数组类型参数 -->
              <template v-else-if="field.type === 'ARRAY'">
                <textarea
                  v-model="params[key]"
                  class="form-control"
                  rows="3"
                  placeholder="每行一个值"
                  @input="updateParams"
                />
              </template>

              <!-- 其他类型参数 -->
              <template v-else>
                <input
                  v-model="params[key]"
                  type="text"
                  class="form-control"
                  :placeholder="field.defaultValue || ''"
                  @input="updateParams"
                >
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 输出参数配置 -->
      <div class="form-group">
        <label class="form-label">输出变量</label>
        <div class="params-list">
          <div
            v-for="(field, key) in toolDefinition.outputFields"
            :key="key"
            class="param-item"
          >
            <div class="param-header">
              <label class="param-label">
                {{ field.name }}
                <span v-if="field.required" class="required">*</span>
              </label>
              <small class="param-type">{{ field.type }}</small>
            </div>
            
            <div class="param-content">
              <small class="param-description">{{ field.description }}</small>
              <input
                v-model="outputVarNames[key]"
                type="text"
                class="form-control"
                :placeholder="'输出变量名称'"
                @input="updateOutputVars"
              >
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { ToolDefinition } from '../types/workflow';
import VariableSelector from './VariableSelector.vue';

const props = defineProps<{
  modelValue?: {
    tool?: string;
    params?: Record<string, any>;
    outputVars?: Record<string, string>;
  };
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void;
  (e: 'change', value: any): void;
}>();

// 状态变量
const selectedTool = ref('');
const params = ref<Record<string, any>>({});
const outputVarNames = ref<Record<string, string>>({});
const paramSource = ref<Record<string, 'static' | 'variable'>>({});

// 模拟可用工具列表，实际应该从API获取
const availableTools = ref([
  {
    name: 'list_directory',
    description: 'List contents of a directory with detailed information',
    inputFields: {
      path: {
        name: 'path',
        description: 'Directory path to list',
        type: 'STRING',
        required: true,
        defaultValue: '.',
      }
    },
    outputFields: {
      entries: {
        name: 'entries',
        description: 'List of directory entries with details',
        type: 'ARRAY',
        required: true,
      }
    }
  }
]);

// 当前选中的工具定义
const toolDefinition = computed(() => {
  return availableTools.value.find(t => t.name === selectedTool.value);
});

// 工具变更处理
function onToolChange() {
  // 重置参数
  params.value = {};
  outputVarNames.value = {};
  paramSource.value = {};
  
  // 设置默认值
  if (toolDefinition.value) {
    Object.entries(toolDefinition.value.inputFields).forEach(([key, field]) => {
      params.value[key] = field.defaultValue || '';
      paramSource.value[key] = 'static';
    });
    
    Object.keys(toolDefinition.value.outputFields).forEach(key => {
      outputVarNames.value[key] = '';
    });
  }
  
  updateValue();
}

// 更新参数
function updateParams() {
  updateValue();
}

// 更新输出变量
function updateOutputVars() {
  updateValue();
}

// 更新值
function updateValue() {
  const value = {
    tool: selectedTool.value,
    params: params.value,
    outputVars: outputVarNames.value
  };
  
  emit('update:modelValue', value);
  emit('change', value);
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    selectedTool.value = newValue.tool || '';
    params.value = newValue.params ? { ...newValue.params } : {};
    outputVarNames.value = newValue.outputVars ? { ...newValue.outputVars } : {};
  }
}, { immediate: true });
</script>

<style scoped>
.tool-params-editor {
  margin-bottom: 1rem;
}

.tool-select {
  width: 100%;
}

.tool-option {
  display: flex;
  flex-direction: column;
}

.tool-name {
  font-weight: bold;
}

.tool-description {
  color: var(--text-color-secondary);
  font-size: 0.875rem;
}

.params-list {
  margin-top: 0.5rem;
}

.param-item {
  padding: 1rem;
  margin-bottom: 1rem;
  background: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.param-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.param-label {
  font-weight: bold;
}

.param-type {
  color: var(--text-color-secondary);
  background: var(--border-color);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
}

.param-description {
  display: block;
  color: var(--text-color-secondary);
  margin-bottom: 0.5rem;
}

.param-input-row {
  display: flex;
  gap: 8px;
}

.param-source-select {
  width: 120px;
}

.required {
  color: #f56c6c;
  margin-left: 4px;
}

.form-control {
  flex: 1;
  background-color: var(--node-bg);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 0.5rem;
  color: var(--text-color);
}

.form-control:focus {
  border-color: var(--node-selected);
  outline: none;
}
</style> 
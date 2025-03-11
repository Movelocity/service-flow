<template>
  <div class="condition-builder">
    <div class="condition-group">
      <el-form :model="condition" label-position="top">
        <div class="condition-row">
          <div class="condition-field">
            <el-select 
              :model-value="condition.leftOperand"
              @update:model-value="updateLeftOperand"
              placeholder="选择变量"
              class="full-width"
            >
              <el-option-group 
                v-for="group in variableGroups" 
                :key="group.label" 
                :label="group.label"
              >
                <el-option
                  v-for="option in group.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                >
                  <span>{{ option.label }}</span>
                  <small class="variable-type">{{ option.type }}</small>
                </el-option>
              </el-option-group>
            </el-select>
          </div>

          <div class="condition-field operator-field">
           
            <el-select 
              :model-value="condition.operator"
              @update:model-value="updateOperator"
              placeholder="选择操作符"
              class="operator-select"
            >
              <el-option
                v-for="op in availableOperators"
                :key="op.value"
                :label="op.label"
                :value="op.value"
              />
            </el-select>
          </div>
        </div>

        <div class="condition-row">
          <div class="condition-field">
            <div class="right-operand-container">
              <div class="operand-input">
                <template v-if="condition.type === 'CONSTANT'">
                  <el-input
                    v-if="selectedVarType === 'string'"
                    :model-value="condition.rightOperand"
                    @update:model-value="updateRightOperand"
                    placeholder="输入文本值"
                    class="operand-input"
                  />
                  <el-input-number
                    v-else-if="selectedVarType === 'number'"
                    :model-value="Number(condition.rightOperand)"
                    @update:model-value="updateRightOperand"
                    :controls="false"
                    placeholder="输入数值"
                  />
                  <el-select
                    v-else-if="selectedVarType === 'boolean'"
                    :model-value="condition.rightOperand"
                    @update:model-value="updateRightOperand"
                    placeholder="选择布尔值"
                  >
                    <el-option label="是" value="true" />
                    <el-option label="否" value="false" />
                  </el-select>
                  <el-input
                    v-else
                    :model-value="condition.rightOperand"
                    @update:model-value="updateRightOperand"
                    placeholder="输入比较值"
                  />
                </template>
                <el-select
                  v-else
                  :model-value="condition.rightOperand"
                  @update:model-value="updateRightOperand"
                  placeholder="选择变量"
                  class="full-width"
                >
                  <el-option-group 
                    v-for="group in variableGroups" 
                    :key="group.label" 
                    :label="group.label"
                  >
                    <el-option
                      v-for="option in group.options"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    >
                      <span>{{ option.label }}</span>
                      <small class="variable-type">{{ option.type }}</small>
                    </el-option>
                  </el-option-group>
                </el-select>
              </div>
              
              <div class="type-select">
                <el-select
                  :model-value="condition.type"
                  @update:model-value="updateType"
                >
                  <el-option label="常量" value="CONSTANT" />
                  <el-option label="变量" value="VARIABLE" />
                </el-select>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </div>
    
    <div class="condition-preview" v-if="showPreview">
      <el-alert
        :title="previewText"
        type="info"
        :closable="false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import type { Condition } from '@/types/condition';

const props = defineProps<{
  modelValue: Condition;
  showPreview?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: Condition];
  'change': [];
}>();

const store = useWorkflowStore();

// 使用计算属性来访问条件值，并确保响应性
const condition = computed(() => ({
  leftOperand: props.modelValue.leftOperand,
  operator: props.modelValue.operator,
  rightOperand: props.modelValue.rightOperand,
  type: props.modelValue.type
}));

// 变量类型映射
const variableTypes = ref(new Map<string, string>());

// 获取变量类型
const getVariableType = (variablePath: string): string => {
  return variableTypes.value.get(variablePath) || 'string';
};

// 当前选中变量的类型
const selectedVarType = computed(() => {
  return getVariableType(condition.value.leftOperand);
});

// 根据变量类型获取可用的操作符
const availableOperators = computed(() => {
  const type = selectedVarType.value;
  
  switch (type) {
    case 'string':
      return [
        { label: '等于', value: '==' },
        { label: '不等于', value: '!=' },
        { label: '包含', value: 'contains' },
        { label: '不包含', value: 'notContains' },
        { label: '开头是', value: 'startsWith' },
        { label: '结尾是', value: 'endsWith' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
      ];
    case 'number':
      return [
        { label: '等于', value: '==' },
        { label: '不等于', value: '!=' },
        { label: '大于', value: '>' },
        { label: '大于等于', value: '>=' },
        { label: '小于', value: '<' },
        { label: '小于等于', value: '<=' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
      ];
    case 'boolean':
      return [
        { label: '等于', value: '==' },
        { label: '不等于', value: '!=' }
      ];
    default:
      return [
        { label: '等于', value: '==' },
        { label: '不等于', value: '!=' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
      ];
  }
});

// 更新处理函数
function emitUpdate(updates: Partial<Condition>) {
  emit('update:modelValue', {
    leftOperand: condition.value.leftOperand,
    operator: condition.value.operator,
    rightOperand: condition.value.rightOperand,
    type: condition.value.type,
    ...updates
  });
  emit('change');
}

function updateLeftOperand(value: string) {
  // 当左操作数改变时，重置操作符和右操作数
  emitUpdate({ 
    leftOperand: value,
    operator: '==',
    rightOperand: ''
  });
}

function updateOperator(value: string) {
  // 如果选择了 isEmpty 或 isNotEmpty，清空右操作数
  if (value === 'isEmpty' || value === 'isNotEmpty') {
    emitUpdate({ 
      operator: value,
      rightOperand: '',
      type: 'CONSTANT'
    });
  } else {
    emitUpdate({ operator: value });
  }
}

function updateType(value: 'VARIABLE' | 'CONSTANT') {
  emitUpdate({ 
    type: value,
    rightOperand: '' // 清空右操作数，因为类型改变了
  });
}

function updateRightOperand(value: string | number) {
  emitUpdate({ rightOperand: String(value) });
}

// 获取工作流输入和变量组
const variableGroups = computed(() => {
  const workflowInputs = store.currentWorkflow?.inputs || {};
  const nodeContext = store.selectedNode?.context || {};
  
  // 更新变量类型映射
  Object.entries(workflowInputs).forEach(([key, value]) => {
    variableTypes.value.set(`global.${key}`, typeof value);
  });
  
  Object.entries(nodeContext).forEach(([key, value]) => {
    variableTypes.value.set(key, typeof value);
  });
  
  return [
    {
      label: '工作流输入',
      options: Object.entries(workflowInputs).map(([key, value]) => ({
        label: key,
        value: `global.${key}`,
        type: typeof value
      }))
    },
    {
      label: '节点上下文',
      options: Object.entries(nodeContext).map(([key, value]) => ({
        label: key,
        value: key,
        type: typeof value
      }))
    }
  ];
});

// 生成预览文本
const previewText = computed(() => {
  const { leftOperand, operator, rightOperand, type } = condition.value;
  if (operator === 'isEmpty') {
    return `${leftOperand} 为空`;
  }
  if (operator === 'isNotEmpty') {
    return `${leftOperand} 不为空`;
  }
  return `${leftOperand} ${operator} ${type === 'CONSTANT' ? `"${rightOperand}"` : rightOperand}`;
});
</script>

<style scoped>
.condition-builder {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1rem 1rem 0 1rem;
  background-color: var(--bg-primary);
}

.condition-row {
  display: flex;
  gap: .5rem;
  margin-bottom: 1rem;
}

.condition-field {
  flex: 1;
}

.operator-field {
  width: 80px;
  flex: 0 0 auto;
}

.operand-input {
  flex: 1;
}

.condition-label {
  display: block;
  font-size: 0.9rem;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.right-operand-container {
  display: flex;
  gap: 0.5rem;
}

.full-width {
  width: 100%;
}

.operator-select {
  width: 100%;
}

.type-select {
  width: 80px;
}

.variable-type {
  margin-left: 8px;
  color: var(--text-color);
  opacity: 0.6;
}

.condition-preview {
  margin-top: 1rem;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-select),
:deep(.el-input),
:deep(.el-input-number) {
  width: 100%;
}
</style> 
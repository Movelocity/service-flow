<template>
  <div class="condition-builder">
    <div class="condition-group">
      <el-form :model="condition" label-position="top">
        <div class="condition-row">
          <div class="condition-field">
            <VariableSelector
              :model-value="condition.leftOperand.parent + '.' + condition.leftOperand.name"
              @update:model-value="updateLeftOperand"
              :available-context="availableContext"
              placeholder="选择变量"
              :full-width="true"
            />
          </div>

          <div class="condition-field operator-field">
           
            <el-select 
              :model-value="condition.operator"
              @change="updateOperator"
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
                    v-if="selectedVarType === VariableType.STRING"
                    :model-value="condition.rightOperand.value"
                    @input="updateRightOperand"
                    placeholder="输入文本"
                    class="operand-input"
                    spellcheck="false"
                  />
                  <el-input-number
                    v-else-if="selectedVarType === VariableType.NUMBER"
                    :model-value="Number(condition.rightOperand.value)"
                    @change="updateRightOperand"
                    :controls="false"
                    placeholder="输入数字"
                  />
                  <el-select
                    v-else-if="selectedVarType === VariableType.BOOLEAN"
                    :model-value="condition.rightOperand.value"
                    @change="updateRightOperand"
                    placeholder="选择布尔值"
                  >
                    <el-option label="是" value="true" />
                    <el-option label="否" value="false" />
                  </el-select>
                  <el-input
                    v-else
                    :model-value="condition.rightOperand.value"
                    @change="updateRightOperand"
                    placeholder="输入比较值"
                    spellcheck="false"
                  />
                </template>
                <VariableSelector
                  v-else
                  :model-value="condition.rightOperand.parent + '.' + condition.rightOperand.name"
                  @update:model-value="updateRightOperand"
                  :available-context="availableContext"
                  placeholder="选择变量"
                  :full-width="true"
                />
              </div>
              
              <div class="type-select">
                <el-select v-model="condition.type" @change="updateType">
                  <el-option label="常量" value="CONSTANT" />
                  <el-option label="变量" value="VARIABLE" />
                </el-select>
              </div>
            </div>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import type { Condition } from '@/types/fields';
import { VariableType } from '@/types/fields';
import { useAvailableContext } from '@/composables/useAvailableContext'
import VariableSelector from '@/components/common/VariableSelector.vue'

const props = defineProps<{
  modelValue: Condition;
  showPreview?: boolean;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: Condition];
  'change': [];
}>();

const store = useWorkflowStore();

const condition = computed(() => ({
  leftOperand: props.modelValue.leftOperand,
  operator: props.modelValue.operator,
  rightOperand: props.modelValue.rightOperand,
  type: props.modelValue.type
}));

// 当前选中变量的类型
const selectedVarType = computed(() => {
  return condition.value.leftOperand.type;
});

// 根据变量类型获取可用的操作符
const availableOperators = computed(() => {
  const type = selectedVarType.value;
  switch (type) {
    case VariableType.STRING:
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
    case VariableType.NUMBER:
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
    case VariableType.BOOLEAN:
      return [
        { label: '等于', value: '==' },
        { label: '不等于', value: '!=' }
      ];
    case VariableType.ARRAY:
      return [
        { label: '包含', value: 'contains' },
        { label: '不包含', value: 'notContains' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
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

// console.log("availableOperators", selectedVarType.value, availableOperators.value)

function updateLeftOperand(value: string, parent: string) {
  const selectedVar = availableContext.value.find(variable => 
    variable.name === value && variable.parent === parent)
  
  if (selectedVar) {
    emitUpdate({
      leftOperand: {
        name: value,
        type: selectedVar.type || VariableType.STRING,
        description: selectedVar.description || '',
        defaultValue: selectedVar.defaultValue,
        parent: parent
      },
      operator: '==',
      rightOperand: {
        name: '',
        type: selectedVar.type || VariableType.STRING,
        description: '',
        defaultValue: '',
        parent: ''
      }
    });
  }
}

function updateOperator(value: string) {
  if (value === 'isEmpty' || value === 'isNotEmpty') {
    emitUpdate({ 
      operator: value,
      rightOperand: {
        name: '',
        type: condition.value.leftOperand.type,
        description: '',
        defaultValue: '',
        parent: ''
      },
      type: 'CONSTANT'
    });
  } else {
    emitUpdate({ operator: value });
  }
}

function updateType(value: 'VARIABLE' | 'CONSTANT') {
  emitUpdate({ 
    type: value,
    rightOperand: {
      name: '',
      type: condition.value.leftOperand.type,
      description: '',
      defaultValue: '',
      parent: ''
    }
  });
}

function updateRightOperand(value: any, parent?: string) {
  console.log("updateRightOperand", condition.value.type, value, typeof value)
  if(condition.value.type === 'CONSTANT') {  // 常量模式
    emitUpdate({ 
      rightOperand: {
        name: "",
        type: condition.value.leftOperand.type,
        description: '',
        value: value,
        parent: ''
      }
    });
  } else {  // 变量模式
    emitUpdate({ 
      rightOperand: {
        name: value,
        type: condition.value.leftOperand.type,
        description: '',
        defaultValue: '',
        parent: parent || ''
      }
    });
  }
}

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

const availableContext = useAvailableContext(store.selectedNode?.id || '')
</script>

<style scoped>
.condition-builder {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.75rem 0.75rem 0 0.75rem;
  background-color: var(--bg-primary);
}

.condition-row {
  display: flex;
  gap: .5rem;
  margin-bottom: 0.75rem;
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

.operator-select {
  width: 100%;
}

.type-select {
  width: 80px;
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
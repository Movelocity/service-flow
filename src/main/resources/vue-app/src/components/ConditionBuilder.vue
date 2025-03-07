<template>
  <div class="condition-builder">
    <div class="condition-row">
      <!-- 变量选择器 -->
      <VariableSelector
        v-model="selectedVariableId"
        placeholder="选择变量"
        @change="onVariableChange"
      />

      <!-- 操作符选择器 -->
      <el-select
        v-if="selectedVariable"
        v-model="selectedOperator"
        class="operator-select"
        @change="updateCondition"
      >
        <el-option
          v-for="op in availableOperators"
          :key="op.value"
          :label="op.label"
          :value="op.value"
        />
      </el-select>

      <!-- 比较值输入 -->
      <template v-if="selectedVariable && needsOperand">
        <template v-if="selectedVariable.type === 'BOOLEAN'">
          <el-select
            v-model="operandValue"
            class="operand-select"
            @change="updateCondition"
          >
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </template>
        <template v-else>
          <input
            v-model="operandValue"
            type="text"
            class="form-control operand-input"
            :placeholder="operandPlaceholder"
            @input="updateCondition"
          >
        </template>
      </template>
    </div>

    <div class="preview-row" v-if="selectedVariable">
      <small class="condition-preview">
        {{ conditionPreview }}
      </small>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { VariableType } from '../types/workflow';
import VariableSelector from './VariableSelector.vue';
import { useWorkflowStore } from '../stores/workflow';

const props = defineProps<{
  modelValue?: string;  // 条件表达式
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: string): void;
}>();

const store = useWorkflowStore();

// 状态变量
const selectedVariableId = ref('');
const selectedOperator = ref('');
const operandValue = ref<any>('');

// 获取选中的变量信息
const selectedVariable = computed(() => {
  if (!selectedVariableId.value) return null;
  
  const [parent, name] = selectedVariableId.value.split(':');
  if (parent === 'global') {
    return store.currentWorkflow?.globalVariables?.find(v => v.name === name);
  } else {
    const node = store.currentWorkflow?.nodes.find(n => n.id === parent);
    return node?.outputVariables?.find(v => v.name === name);
  }
});

// 根据变量类型获取可用的操作符
const availableOperators = computed(() => {
  if (!selectedVariable.value) return [];

  switch (selectedVariable.value.type) {
    case VariableType.STRING:
      return [
        { label: '包含', value: 'contains' },
        { label: '不包含', value: 'notContains' },
        { label: '开始是', value: 'startsWith' },
        { label: '结束是', value: 'endsWith' },
        { label: '是', value: 'equals' },
        { label: '不是', value: 'notEquals' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
      ];
    case VariableType.NUMBER:
      return [
        { label: '等于', value: '=' },
        { label: '不等于', value: '!=' },
        { label: '大于', value: '>' },
        { label: '小于', value: '<' },
        { label: '大于等于', value: '>=' },
        { label: '小于等于', value: '<=' },
        { label: '为空', value: 'isEmpty' },
        { label: '不为空', value: 'isNotEmpty' }
      ];
    case VariableType.BOOLEAN:
      return [
        { label: '是', value: 'true' },
        { label: '否', value: 'false' }
      ];
    default:
      return [];
  }
});

// 是否需要比较值
const needsOperand = computed(() => {
  if (!selectedOperator.value) return false;
  return !['isEmpty', 'isNotEmpty', 'true', 'false'].includes(selectedOperator.value);
});

// 比较值的占位符
const operandPlaceholder = computed(() => {
  if (!selectedVariable.value) return '';
  
  switch (selectedVariable.value.type) {
    case VariableType.STRING:
      return '请输入文本';
    case VariableType.NUMBER:
      return '请输入数字';
    default:
      return '';
  }
});

// 条件预览
const conditionPreview = computed(() => {
  if (!selectedVariable.value || !selectedOperator.value) return '';

  const varName = selectedVariableId.value.split(':')[1];
  
  switch (selectedOperator.value) {
    case 'isEmpty':
      return `${varName} 为空`;
    case 'isNotEmpty':
      return `${varName} 不为空`;
    case 'true':
      return `${varName} 为真`;
    case 'false':
      return `${varName} 为假`;
    default:
      const opLabel = availableOperators.value.find(op => op.value === selectedOperator.value)?.label;
      return `${varName} ${opLabel} ${operandValue.value}`;
  }
});

// 生成条件表达式
function generateCondition(): string {
  if (!selectedVariable.value || !selectedOperator.value) return '';

  const varPath = selectedVariableId.value.replace(':', '.');
  
  switch (selectedOperator.value) {
    case 'isEmpty':
      return `!${varPath}`;
    case 'isNotEmpty':
      return `!!${varPath}`;
    case 'contains':
      return `${varPath}.includes("${operandValue.value}")`;
    case 'notContains':
      return `!${varPath}.includes("${operandValue.value}")`;
    case 'startsWith':
      return `${varPath}.startsWith("${operandValue.value}")`;
    case 'endsWith':
      return `${varPath}.endsWith("${operandValue.value}")`;
    case 'equals':
      return `${varPath} === "${operandValue.value}"`;
    case 'notEquals':
      return `${varPath} !== "${operandValue.value}"`;
    case 'true':
      return `${varPath}`;
    case 'false':
      return `!${varPath}`;
    default:
      // 数字比较
      return `${varPath} ${selectedOperator.value} ${operandValue.value}`;
  }
}

// 更新条件
function updateCondition() {
  const condition = generateCondition();
  emit('update:modelValue', condition);
  emit('change', condition);
}

// 变量选择变更处理
function onVariableChange() {
  selectedOperator.value = '';
  operandValue.value = '';
  updateCondition();
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (!newValue) {
    selectedVariableId.value = '';
    selectedOperator.value = '';
    operandValue.value = '';
  }
}, { immediate: true });
</script>

<style scoped>
.condition-builder {
  margin-bottom: 1rem;
}

.condition-row {
  display: flex;
  gap: 8px;
  margin-bottom: 0.5rem;
}

.operator-select {
  width: 120px;
}

.operand-input,
.operand-select {
  width: 150px;
}

.preview-row {
  margin-top: 0.5rem;
}

.condition-preview {
  color: var(--text-color-secondary);
  font-style: italic;
}
</style> 
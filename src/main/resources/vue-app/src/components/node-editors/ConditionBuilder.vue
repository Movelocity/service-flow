<template>
  <div class="condition-builder">
    <div class="condition-group">
      <el-form :model="localCondition" label-position="top">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="变量">
              <el-select v-model="localCondition.leftOperand" placeholder="选择变量">
                <el-option-group label="工作流输入">
                  <el-option
                    v-for="(input, key) in workflowInputs"
                    :key="`input:${key}`"
                    :label="input.description || key"
                    :value="`input:${key}`"
                  />
                </el-option-group>
                <el-option-group label="全局变量">
                  <el-option
                    v-for="(global, key) in globalVariables"
                    :key="`global:${key}`"
                    :label="key"
                    :value="`global:${key}`"
                  />
                </el-option-group>
                <el-option-group label="节点输出">
                  <el-option
                    v-for="node in previousNodes"
                    :key="`${node.id}:output`"
                    :label="node.name"
                    :value="`${node.id}:output`"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="操作符">
              <el-select v-model="localCondition.operator" placeholder="选择操作符">
                <el-option label="等于" value="==" />
                <el-option label="不等于" value="!=" />
                <el-option label="大于" value=">" />
                <el-option label="大于等于" value=">=" />
                <el-option label="小于" value="<" />
                <el-option label="小于等于" value="<=" />
                <el-option label="包含" value="contains" />
                <el-option label="不包含" value="notContains" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="比较值">
              <div class="value-input">
                <el-radio-group v-model="localCondition.type" size="small">
                  <el-radio-button label="CONSTANT">常量</el-radio-button>
                  <el-radio-button label="VARIABLE">变量</el-radio-button>
                </el-radio-group>
                <el-input
                  v-if="localCondition.type === 'CONSTANT'"
                  v-model="localCondition.rightOperand"
                  placeholder="输入比较值"
                />
                <el-select
                  v-else
                  v-model="localCondition.rightOperand"
                  placeholder="选择变量"
                >
                  <el-option-group label="工作流输入">
                    <el-option
                      v-for="(input, key) in workflowInputs"
                      :key="`input:${key}`"
                      :label="input.description || key"
                      :value="`input:${key}`"
                    />
                  </el-option-group>
                  <el-option-group label="全局变量">
                    <el-option
                      v-for="(global, key) in globalVariables"
                      :key="`global:${key}`"
                      :label="key"
                      :value="`global:${key}`"
                    />
                  </el-option-group>
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    
    <div class="condition-preview">
      <el-alert
        :title="previewText"
        type="info"
        :closable="false"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import type { Node } from '@/types/workflow';

interface Condition {
  leftOperand: string;
  operator: string;
  rightOperand: string;
  type: 'VARIABLE' | 'CONSTANT';
}

const props = defineProps<{
  modelValue: Condition;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: Condition];
  'change': [];
}>();

const store = useWorkflowStore();

const localCondition = ref<Condition>({
  leftOperand: props.modelValue?.leftOperand || '',
  operator: props.modelValue?.operator || '==',
  rightOperand: props.modelValue?.rightOperand || '',
  type: props.modelValue?.type || 'CONSTANT'
});

// 监听本地条件变化并更新
watch(localCondition, (newValue) => {
  emit('update:modelValue', { ...newValue });
  emit('change');
}, { deep: true });

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    localCondition.value = { ...newValue };
  }
}, { deep: true });

// 获取工作流输入
const workflowInputs = computed(() => store.currentWorkflow?.inputs || {});

// 获取全局变量
const globalVariables = computed(() => store.currentWorkflow?.globalVariables || {});

// 获取前置节点列表
const previousNodes = computed(() => {
  const currentNodeId = store.selectedNode?.id;
  return (store.currentWorkflow?.nodes || []).filter((node: Node) => node.id !== currentNodeId);
});

// 生成预览文本
const previewText = computed(() => {
  const { leftOperand, operator, rightOperand, type } = localCondition.value;
  const operatorText = {
    '==': '等于',
    '!=': '不等于',
    '>': '大于',
    '>=': '大于等于',
    '<': '小于',
    '<=': '小于等于',
    'contains': '包含',
    'notContains': '不包含'
  }[operator] || operator;

  return `当 ${leftOperand} ${operatorText} ${type === 'CONSTANT' ? `"${rightOperand}"` : rightOperand} 时`;
});
</script>

<style scoped>
.condition-builder {
  padding: 1rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.condition-group {
  margin-bottom: 1rem;
}

.value-input {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.condition-preview {
  margin-top: 1rem;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-radio-group) {
  margin-bottom: 0.5rem;
}
</style> 
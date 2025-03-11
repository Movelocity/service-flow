<template>
  <div class="condition-builder">
    <div class="condition-group">
      <el-form :model="condition" label-position="top">
        <div class="compare1">
          <el-select 
            :model-value="condition.leftOperand"
            @update:model-value="updateLeftOperand"
            placeholder="选择变量"
            style="flex: 1"
          >
            <el-option-group label="工作流输入">
              <el-option
                v-for="(input, key) in workflowInputs"
                :key="`input.${key}`"
                :label="key"
                :value="`global.${key}`"
              />
            </el-option-group>
            <el-option-group label="节点上下文">
              <el-option
                v-for="(value, key) in nodeContext"
                :key="key"
                :label="key"
                :value="key"
              />
            </el-option-group>
          </el-select>
          <el-select 
            :model-value="condition.operator"
            @update:model-value="updateOperator"
            placeholder="选择操作符"
            style="width: 120px"
          >
            <el-option label="等于" value="==" />
            <el-option label="不等于" value="!=" />
            <el-option label="大于" value=">" />
            <el-option label="大于等于" value=">=" />
            <el-option label="小于" value="<" />
            <el-option label="小于等于" value="<=" />
            <el-option label="包含" value="contains" />
            <el-option label="不包含" value="notContains" />
          </el-select>
        </div>

        <div class="compare2">
          <div style="flex: 1">
            <el-input
              v-if="condition.type === 'CONSTANT'"
              :model-value="condition.rightOperand"
              @update:model-value="updateRightOperand"
              placeholder="输入比较值"
            />
            <el-select
              v-else
              :model-value="condition.rightOperand"
              @update:model-value="updateRightOperand"
              placeholder="选择变量"
              style="width: 100%"
            >
              <el-option-group label="工作流输入">
                <el-option
                  v-for="(input, key) in workflowInputs"
                  :key="`input.${key}`"
                  :label="key"
                  :value="`global.${key}`"
                />
              </el-option-group>
              <el-option-group label="节点上下文">
                <el-option
                  v-for="(value, key) in nodeContext"
                  :key="key"
                  :label="key"
                  :value="key"
                />
              </el-option-group>
            </el-select>
          </div>

          <el-select
            :model-value="condition.type"
            @update:model-value="updateType"
            placeholder="选择类型"
            style="width: 120px"
          >
            <el-option label="常量" value="CONSTANT" />
            <el-option label="变量" value="VARIABLE" />
          </el-select>
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
import { computed } from 'vue';
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
  emitUpdate({ leftOperand: value });
}

function updateOperator(value: string) {
  emitUpdate({ operator: value });
}

function updateType(value: 'VARIABLE' | 'CONSTANT') {
  emitUpdate({ 
    type: value,
    rightOperand: '' // 清空右操作数，因为类型改变了
  });
}

function updateRightOperand(value: string) {
  emitUpdate({ rightOperand: value });
}

// 获取工作流输入
const workflowInputs = computed(() => store.currentWorkflow?.inputs || {});

// 获取节点上下文
const nodeContext = computed(() => store.selectedNode?.context || {});

// 生成预览文本
const previewText = computed(() => {
  const { leftOperand, operator, rightOperand, type } = condition.value;
  return `${leftOperand} ${operator} ${type === 'CONSTANT' ? `"${rightOperand}"` : rightOperand}`;
});
</script>

<style scoped>
.condition-builder {
  border-radius: 4px;
  padding: 1rem;
  background-color: var(--bg-secondary);
}

.condition-group {
  margin-bottom: 1rem;
}

.compare1, .compare2 {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: start;
  margin-bottom: 0.5rem;
}

.compare2 {
  border-top: 1px solid var(--border-color);
  padding-top: 0.5rem;
}

.condition-preview {
  margin-top: 1rem;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-select) {
  width: auto;
}

:deep(.el-input) {
  width: 100%;
}
</style> 
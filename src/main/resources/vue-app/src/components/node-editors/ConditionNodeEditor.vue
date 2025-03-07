<template>
  <div class="form-group">
    <label class="form-label">条件配置</label>
    <ConditionBuilder
      v-model="modelValue.parameters.condition"
      @change="updateCondition"
    />
    <small class="form-text text-muted">
      选择变量并设置条件，支持字符串、数字和布尔值类型的比较
    </small>
  </div>
</template>

<script setup lang="ts">
import type { Node } from '@/types/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';

const props = defineProps<{
  modelValue: Node;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Node): void;
}>();

function updateCondition() {
  emit('update:modelValue', { ...props.modelValue });
}
</script>

<style scoped>
.form-text {
  font-size: 0.875rem;
  margin-top: 0.25rem;
  color: var(--text-color);
  opacity: 0.7;
}
</style> 
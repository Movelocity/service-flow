<template>
  <el-select 
    :model-value="modelValue"
    @update:modelValue="(val: string) => $emit('update:modelValue', val)"
    :placeholder="placeholder"
    :class="{ 'full-width': fullWidth }"
  >
    <el-option
      v-for="variable in availableContext"
      :key="variable.name"
      :label="variable.name"
      :value="variable.name"
    >
      <span>{{ variable.name }}</span>
      <span class="variable-type">{{ variable.type }}</span>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import type { VariableDefinition } from '@/types/fields'

defineProps<{
  modelValue: string
  availableContext: VariableDefinition[]
  placeholder?: string
  fullWidth?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<style scoped>
.full-width {
  width: 100%;
}

.variable-type {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style> 
<template>
  <el-select 
    :model-value="modelValue"
    @update:modelValue="handleUpdate"
    :placeholder="placeholder"
    :class="{ 'full-width': fullWidth }"
  >
    <!-- 分组变量 -->
    <el-option-group
      v-for="group in groupedVariables"
      :key="group.parent"
      :label="group.parentName"
    >
      <el-option
        v-for="variable in group.variables"
        :key="variable.name"
        :label="variable.name"
        :value="variable.parent + '.' + variable.name"
      >
        <span>{{ variable.name }}</span>
        <span class="variable-type">{{ variable.type }}</span>
      </el-option>
    </el-option-group>
  </el-select>
</template>

<script setup lang="ts">
import type { VariableDef } from '@/types/fields'
import { computed } from 'vue'
import { useWorkflowStore } from '@/stores/workflow'

const store = useWorkflowStore()

const props = defineProps<{
  modelValue: string
  availableContext: VariableDef[]
  placeholder?: string
  fullWidth?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string, parent: string]
}>()

// 处理选择更新，提取父级ID和变量名
const handleUpdate = (val: string) => {
  // 从选择的值中提取父级ID和变量名
  const parts = val.split('.')
  const parentId = parts[0]
  const variableName = parts.slice(1).join('.')
  // 发送更新事件，同时传递变量名和父级ID
  emit('update:modelValue', variableName, parentId)
}

interface GroupedVariables {
  parent: string
  parentName: string
  variables: VariableDef[]
}

// 未分组变量（没有父级）
// const ungroupedVariables = computed(() => {
//   return props.availableContext.filter(v => !v.parent)
// })

// 分组变量
const groupedVariables = computed(() => {
  const groups = new Map<string, GroupedVariables>()
  
  // 第一次遍历：收集所有有父级的变量
  props.availableContext.forEach(variable => {
    if (variable.parent) {
      if (!groups.has(variable.parent)) {
        // 通过节点ID查找对应的工作流节点
        const parentNode = store.currentWorkflow?.nodes.find(node => node.id === variable.parent)
        groups.set(variable.parent, {
          parent: variable.parent,
          parentName: parentNode?.name || variable.parent, // 如果找不到节点，则使用ID作为后备
          variables: []
        })
      }
      groups.get(variable.parent)?.variables.push(variable)
    }
  })

  return Array.from(groups.values())
})
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
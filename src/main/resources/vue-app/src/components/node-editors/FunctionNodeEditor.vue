<template>
  <div class="form-group">
    <ToolParamsEditor
      v-model="modelValue.parameters"
      @change="updateParams"
    />
  </div>
</template>

<script setup lang="ts">
import type { Node } from '@/types/workflow';
import ToolParamsEditor from '@/components/node-editors/ToolParamsEditor.vue';

const props = defineProps<{
  modelValue: Node;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Node): void;
}>();

function updateParams() {
  emit('update:modelValue', { ...props.modelValue });
}
</script> 
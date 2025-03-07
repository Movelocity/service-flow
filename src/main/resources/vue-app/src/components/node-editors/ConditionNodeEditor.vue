<template>
  <div class="form-group">
    <label class="form-label">条件配置</label>
    <ConditionBuilder
      v-if="selectedNode"
      v-model="selectedNode.parameters.condition"
      @change="updateCondition"
    />
    <small class="form-text text-muted">
      选择变量并设置条件，支持字符串、数字和布尔值类型的比较
    </small>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import ConditionBuilder from '@/components/node-editors/ConditionBuilder.vue';

const store = useWorkflowStore();
const selectedNode = computed(() => store.selectedNode);

function updateCondition() {
  if (selectedNode.value) {
    store.updateNode(selectedNode.value.id, selectedNode.value);
  }
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
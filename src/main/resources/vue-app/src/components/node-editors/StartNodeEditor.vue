<template>
  <div class="form-group">
    <div class="variable-row">
      <label class="form-label">工作流参数</label>
      <div>
        <el-button
          type="primary"
          size="small"
          @click="addVariable"
        >
          +
        </el-button>
      </div>
    </div>

    <div class="variables-list">
      <div v-for="(variable, index) in modelValue.parameters.globalVariables || []" :key="index" class="variable-row">
        <span>{{ variable.name }}</span>
        <div>
          <span>{{ variable.type }}</span>
          <el-button type="primary" link @click="editVariable(index)"> 编辑 </el-button>
        </div>
      </div>
    </div>
  </div>

  <VariableEditor
    v-model="editingVariable"
    v-model:visible="variableDialogVisible"
    @save="saveVariable"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { Node } from '@/types/workflow';
import { VariableType } from '@/types/workflow';
import VariableEditor from '@/components/VariableEditor.vue';

const props = defineProps<{
  modelValue: Node;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Node): void;
}>();

const variableDialogVisible = ref(false);
const editingVariableIndex = ref(-1);
const editingVariable = ref<any>(null);

// 添加全局变量
function addVariable() {
  editingVariableIndex.value = -1;
  editingVariable.value = {
    name: '',
    type: VariableType.STRING,
    description: '',
    required: false,
    defaultValue: ''
  };
  variableDialogVisible.value = true;
}

function editVariable(index: number) {
  editingVariableIndex.value = index;
  editingVariable.value = JSON.parse(
    JSON.stringify(props.modelValue.parameters.globalVariables[index])
  );
  variableDialogVisible.value = true;
}

function saveVariable() {
  if (!editingVariable.value) return;
  
  const updatedNode = { ...props.modelValue };
  if (!updatedNode.parameters.globalVariables) {
    updatedNode.parameters.globalVariables = [];
  }

  if (editingVariableIndex.value === -1) {
    updatedNode.parameters.globalVariables.push(editingVariable.value);
  } else {
    updatedNode.parameters.globalVariables[editingVariableIndex.value] = editingVariable.value;
  }

  emit('update:modelValue', updatedNode);
}
</script>

<style scoped>
.variables-list {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.variable-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 0.5rem;
  align-items: center;
}
</style> 
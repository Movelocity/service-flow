<template>
  <div class="start-node-editor">
    <h3>工作流输入变量</h3>
    <div class="variables-list">
      <div v-for="(def, key) in workflow?.inputs" :key="key" class="variable-row">
        <span>{{ key }}</span>
        <div>
          <span>{{ def.type }}</span>
          <span v-if="def.description" class="description">{{ def.description }}</span>
          <el-button type="primary" link @click="editVariable(String(key))">编辑</el-button>
          <el-button type="danger" link @click="deleteVariable(String(key))">删除</el-button>
        </div>
      </div>
      <el-button type="primary" @click="addVariable">添加输入变量</el-button>
    </div>

    <VariableModal
      v-model="editingVariable"
      v-model:visible="showVariableModal"
      @save="handleVariableSave"
      @close="handleModalClose"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useWorkflowStore } from '@/stores/workflow';
import type { VariableDefinition } from '@/types/workflow';
import { VariableType } from '@/types/workflow';
import VariableModal from '../VariableModal.vue';

interface EditingVariable extends VariableDefinition {
  name: string;
}

const store = useWorkflowStore();
const workflow = computed(() => store.currentWorkflow);
const showVariableModal = ref(false);
const editingVariable = ref<EditingVariable | null>(null);

const addVariable = () => {
  editingVariable.value = {
    name: '',
    type: VariableType.STRING,
    description: '',
    defaultValue: undefined
  };
  showVariableModal.value = true;
};

const editVariable = (key: string) => {
  const def = workflow.value?.inputs[key];
  if (def) {
    editingVariable.value = {
      name: key,
      ...def
    };
    showVariableModal.value = true;
  }
};

const deleteVariable = (key: string) => {
  if (workflow.value) {
    const { [key]: _, ...rest } = workflow.value.inputs;
    workflow.value.inputs = rest;
  }
};

const handleVariableSave = () => {
  if (!workflow.value || !editingVariable.value) return;

  const { name, ...def } = editingVariable.value;

  const oldName = editingVariable.value.name;
  if (oldName && oldName !== name) {
    // 如果是编辑现有变量，且名称改变了，需要删除旧的并添加新的
    const { [oldName]: _, ...rest } = workflow.value.inputs;
    workflow.value.inputs = {
      ...rest,
      [name]: def
    };
  } else {
    // 添加新变量或更新现有变量
    workflow.value.inputs = {
      ...workflow.value.inputs,
      [name]: def
    };
  }
};

const handleModalClose = () => {
  editingVariable.value = null;
};
</script>

<style scoped>
.start-node-editor {
  padding: 1rem;
}

.variables-list {
  margin-top: 1rem;
}

.variable-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.variable-row:last-child {
  border-bottom: none;
}

.description {
  color: #666;
  font-size: 0.875rem;
  margin: 0 0.5rem;
}
</style> 
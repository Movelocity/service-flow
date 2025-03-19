<template>
  <div class="start-node-editor">
    <div class="row">
      <h3>工作流输入变量</h3>
      <el-button type="primary" @click="addVariable">新增</el-button>
    </div>
    <div class="variables">
      <div v-for="def in workflow?.inputs" :key="def.name" class="variable-row">
        <div class="row">
          <span>{{ def.name }}</span>
          <span class="variable-type">{{ def.type }}</span>
        </div>
        <div class="actions">
          <el-icon @click="editVariable(def)" class="icon-btn">
            <Edit />
          </el-icon>
          <el-icon @click="deleteVariable(def.name)" class="icon-btn">
            <Delete />
          </el-icon>
        </div>
      </div>
      
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
import type { VariableDef } from '@/types/fields';
import { VariableType } from '@/types/fields';
import VariableModal from '../VariableModal.vue';
import { Delete, Edit } from '@element-plus/icons-vue';

interface EditingVariable extends VariableDef {
  name: string;
  originalName?: string;
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

const editVariable = (variable: VariableDef) => {
  editingVariable.value = { 
    ...variable,
    originalName: variable.name 
  };
  showVariableModal.value = true;
};

const deleteVariable = (name: string) => {
  if (workflow.value) {
    workflow.value.inputs = workflow.value.inputs.filter(input => input.name !== name);
  }
};

const handleVariableSave = () => {
  if (!workflow.value || !editingVariable.value) return;

  const newVariable = { ...editingVariable.value };
  const originalName = editingVariable.value.originalName;
  delete newVariable.originalName;  // Remove originalName before saving
  
  const existingIndex = workflow.value.inputs.findIndex(
    input => input.name === (originalName ?? newVariable.name)
  );

  if (existingIndex >= 0) {
    // Update existing variable
    workflow.value.inputs[existingIndex] = newVariable;
  } else {
    // Add new variable
    workflow.value.inputs.push(newVariable);
  }
};

const handleModalClose = () => {
  editingVariable.value = null;
};
</script>

<style scoped>
.start-node-editor {
  padding: 1rem 0;
}

.variable-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

.description {
  color: #666;
  font-size: 0.875rem;
  margin: 0 0.5rem;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
}
</style> 
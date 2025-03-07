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
      <div v-for="(variable, index) in node.parameters.inputs || []" :key="index" class="variable-row">
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
import { ref, computed } from 'vue';
import type { Node } from '@/types/workflow';
import { VariableType } from '@/types/workflow';
import VariableEditor from '@/components/VariableModal.vue';
import { useWorkflowStore } from '@/stores/workflow';

const store = useWorkflowStore();
const node = computed(() => store.currentWorkflow?.nodes.find(n => n.id === store.editorState.selectedNodeId) as Node);

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
    JSON.stringify(node.value.parameters.inputs[index])
  );
  variableDialogVisible.value = true;
}

function saveVariable() {
  if (!editingVariable.value || !node.value) return;
  
  const updatedNode = { ...node.value };
  if (!updatedNode.parameters.inputs) {
    updatedNode.parameters.inputs = [];
  }
  if (!updatedNode.outputs) {
    updatedNode.outputs = [];
  }

  if (editingVariableIndex.value === -1) {
    updatedNode.parameters.inputs.push(editingVariable.value);
    // Also add as output variable
    updatedNode.outputs.push({
      ...editingVariable.value
    });
  } else {
    updatedNode.parameters.inputs[editingVariableIndex.value] = editingVariable.value;
    // Update corresponding output variable
    updatedNode.outputs[editingVariableIndex.value] = {
      ...editingVariable.value
    };
  }

  store.updateNode(node.value.id, updatedNode);
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
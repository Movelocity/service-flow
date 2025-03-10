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
      <div v-for="(variable, key) in startNode.outputs || []" :key="key" class="variable-row">
        <span>{{ variable.name }}</span>
        <div>
          <span>{{ variable.type }}</span>
          <el-button type="primary" link @click="editVariable(key)"> 编辑 </el-button>
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
import { NodeType } from '@/types/workflow';
import VariableEditor from '@/components/VariableModal.vue';
import { useWorkflowStore } from '@/stores/workflow';

const store = useWorkflowStore();
const startNode = computed(() => store.currentWorkflow?.nodes.find(n => n.type === NodeType.START) as Node);

const variableDialogVisible = ref(false);
const editingVariableIndex = ref(-1);
const editingVariable = ref<any>(null);

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

function editVariable(key: string) {
  editingVariable.value = JSON.parse(
    JSON.stringify(startNode.value.outputs[key])
  );
  variableDialogVisible.value = true;
}

function saveVariable() {
  if (!editingVariable.value || !startNode.value) return;
  
  const updatedNode = { ...startNode.value };
  if (!updatedNode.outputs) {
    updatedNode.outputs = {};
  }

  // 开始节点只需要配置输出变量；工作流输入变量引用开始节点的输出变量
  if (editingVariableIndex.value === -1) {
    updatedNode.outputs[editingVariable.value.name] = editingVariable.value;
  } else {
    updatedNode.outputs[editingVariableIndex.value] = editingVariable.value;
  }

  store.updateNode(startNode.value.id, updatedNode);
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
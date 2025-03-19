<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isNewVariable ? '添加变量' : '编辑变量'"
    width="500px"
    :modal-class="'variable-dialog'"
    :append-to-body="true"
    destroy-on-close
    style="background-color: var(--node-bg);"
  >
    <div v-if="localVariable" class="variable-form">
      <div class="form-group mb-3">
        <label class="form-label">变量名称</label>
        <el-input
          v-model="localVariable.name"
          placeholder="请输入变量名称"
        />
      </div>
      <div class="form-group mb-3">
        <label class="form-label">类型</label>
        <el-select
          v-model="localVariable.type"
          class="w-100"
        >
          <el-option
            v-for="type in variableTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
      </div>
      <div class="form-group mb-3">
        <label class="form-label">描述</label>
        <el-input
          v-model="localVariable.description"
          type="textarea"
          placeholder="请输入变量描述"
        />
      </div>
      <div class="form-group mb-3">
        <label class="form-label">默认值</label>
        <el-input
          v-model="localVariable.defaultValue"
          placeholder="请输入默认值"
        />
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import type { VariableDef } from '@/types/fields';
import { VariableType } from '@/types/fields';

interface EditingVariable extends VariableDef {
  name: string;
}

// Props
const props = defineProps<{
  modelValue: EditingVariable | null;
  visible: boolean;
}>();

// Emits
const emit = defineEmits<{
  (e: 'update:modelValue', value: EditingVariable | null): void;
  (e: 'save'): void;
  (e: 'close'): void;
  (e: 'update:visible', value: boolean): void;
}>();

// 变量类型选项
const variableTypes = Object.values(VariableType);

// Local state
const localVariable = ref<EditingVariable | null>(null);
const isNewVariable = computed(() => !props.modelValue?.name);

// Computed for dialog visibility with two-way binding
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => {
    emit('update:visible', value);
    if (!value) {
      emit('close');
    }
  }
});

// Watch for changes in modelValue
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    localVariable.value = JSON.parse(JSON.stringify(newValue));
  } else {
    localVariable.value = null;
  }
}, { immediate: true });

// Methods
function handleSave() {
  if (localVariable.value) {
    emit('update:modelValue', localVariable.value);
    emit('save');
  }
  dialogVisible.value = false;
}

function handleClose() {
  dialogVisible.value = false;
}
</script>

<style scoped>
.variable-form {
  padding: 1rem;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

:deep(.el-select) {
  width: 100%;
}

.el-dialog {
  background-color: var(--card-bg);
}
</style>



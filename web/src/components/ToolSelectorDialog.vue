<!-- Tool selector dialog component -->
<template>
  <el-dialog
    v-model="dialog"
    title="选择工具"
    width="600px"
    style="background-color: var(--node-bg);"
    :close-on-click-modal="false"
    class="theme-dark"
  >
    <div class="dialog-header">
      <el-input
        v-model="search"
        placeholder="搜索工具"
        :suffix-icon="Search"
        class="search-input"
      />
    </div>

    <el-scrollbar height="400px">
      <el-space direction="vertical" fill class="tool-list">
        <div
          v-for="tool in filteredTools"
          :key="tool.name"
          class="tool-item"
          @click="selectTool(tool)"
        >
          <h3>{{ tool.name }}</h3>
          <p class="tool-description">{{ tool.description }}</p>
        </div>
      </el-space>
    </el-scrollbar>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { toolApi } from '@/services/toolApi';
const props = defineProps<{
  modelValue: boolean
}>();

const emit = defineEmits<{
  'tool-selected': [toolDetails: any],
  'update:modelValue': [value: boolean]
}>();

const tools = ref<{ name: string; description: string }[]>([]);
const search = ref('');
const loading = ref(false);

// Computed property for dialog v-model
const dialog = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Filter tools based on search text
const filteredTools = computed(() => {
  const searchText = search.value.toLowerCase();
  return tools.value.filter(tool => 
    tool.name.toLowerCase().includes(searchText) ||
    tool.description.toLowerCase().includes(searchText)
  );
});

// Load tools when dialog opens
const loadTools = async () => {
  if (tools.value.length === 0 && !loading.value) {
    loading.value = true;
    try {
      tools.value = await toolApi.listTools();
    } catch (error) {
      console.error('Failed to load tools:', error);
    } finally {
      loading.value = false;
    }
  }
};

// Handle tool selection
const selectTool = async (tool: { name: string; description: string }) => {
  try {
    // Get detailed tool information before emitting selection
    const toolDetails = await toolApi.getToolDetails(tool.name);
    emit('tool-selected', toolDetails);
    closeDialog();
  } catch (error) {
    console.error('Failed to get tool details:', error);
  }
};

// Close dialog
const closeDialog = () => {
  dialog.value = false;
};

// Watch dialog opening to load tools
watch(() => props.modelValue, (newValue: boolean) => {
  if (newValue) {
    loadTools();
  }
});
</script>

<style scoped>
.dialog-header {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.tool-list {
  width: 100%;
}

.tool-item {
  width: 100%;
  padding: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tool-item:hover {
  background-color: var(--el-fill-color-light);
}

.tool-item h3 {
  margin: 0;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.tool-description {
  margin: 4px 0 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style> 
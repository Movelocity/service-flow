<!-- Tool selector dialog component -->
<template>
  <v-dialog v-model="dialog" max-width="600px">
    <v-card>
      <v-card-title>
        选择工具
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="搜索工具"
          single-line
          hide-details
          class="ml-4"
        ></v-text-field>
      </v-card-title>

      <v-card-text>
        <v-list>
          <v-list-item
            v-for="tool in filteredTools"
            :key="tool.name"
            @click="selectTool(tool)"
          >
            <v-list-item-content>
              <v-list-item-title>{{ tool.name }}</v-list-item-title>
              <v-list-item-subtitle>{{ tool.description }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="grey darken-1" text @click="closeDialog">取消</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch } from 'vue';
import { toolApi } from '../services/toolApi';

export default defineComponent({
  name: 'ToolSelectorDialog',
  
  emits: ['tool-selected', 'update:modelValue'],
  
  props: {
    modelValue: {
      type: Boolean,
      required: true
    }
  },

  setup(props, { emit }) {
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

    return {
      dialog,
      tools,
      search,
      loading,
      filteredTools,
      selectTool,
      closeDialog
    };
  }
});
</script>

<style scoped>
.v-list-item {
  cursor: pointer;
}
.v-list-item:hover {
  background-color: rgba(0, 0, 0, 0.04);
}
</style> 
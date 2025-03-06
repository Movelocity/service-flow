<template>
  <div class="workflow-editor">
    <!-- 工作流编辑器的主要内容 -->
    <div class="workflow-canvas">
      <!-- 这里是工作流图的渲染区域 -->
      <!-- 实际项目中可能使用 jsplumb 或其他图形库 -->
    </div>

    <!-- 工具栏 -->
    <div class="workflow-toolbar">
      <v-btn
        color="primary"
        @click="showToolSelector"
      >
        添加函数节点
      </v-btn>
    </div>

    <!-- 工具选择对话框 -->
    <ToolSelectorDialog
      v-model="showToolDialog"
      @tool-selected="handleToolSelected"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import ToolSelectorDialog from './ToolSelectorDialog.vue';
import type { Tool } from '../services/toolApi';

export default defineComponent({
  name: 'WorkflowEditor',

  components: {
    ToolSelectorDialog
  },

  setup() {
    const showToolDialog = ref(false);

    // 显示工具选择对话框
    const showToolSelector = () => {
      showToolDialog.value = true;
    };

    // 处理工具选择
    const handleToolSelected = (tool: Tool) => {
      // 在这里创建新的函数节点
      console.log('Selected tool:', tool);
      // TODO: 根据选择的工具创建对应的函数节点
      // 1. 创建节点
      // 2. 设置节点属性（名称、输入输出端口等）
      // 3. 添加到画布
    };

    return {
      showToolDialog,
      showToolSelector,
      handleToolSelected
    };
  }
});
</script>

<style scoped>
.workflow-editor {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: column;
}

.workflow-canvas {
  flex: 1;
  background-color: #f5f5f5;
  position: relative;
}

.workflow-toolbar {
  padding: 8px;
  border-top: 1px solid #e0e0e0;
  background-color: #ffffff;
}
</style> 
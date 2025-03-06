# 工作流组件优化

本目录包含了优化后的工作流相关组件。

## 优化内容

1. **组件拆分与重命名**：
   - 将原有的大型组件拆分为更小、更专注的组件
   - 简化组件命名，去掉冗长的"Workflow"前缀
   - 按照功能职责进行组件划分

2. **目录结构优化**：
   - 将工作流相关组件从通用components目录移至专门的workflow目录
   - 在workflow目录下创建components子目录，专门存放UI组件
   - 遵循推荐的项目结构规范

3. **组件功能优化**：
   - 每个组件专注于单一职责
   - 清晰的组件接口设计
   - 更好的代码组织和注释

## 组件结构

### 核心组件

- **Editor.vue** - 工作流编辑器的主组件，集成了所有其他组件
- **Canvas.vue** - 工作流画布，用于显示和交互节点和连接
- **Node.vue** - 工作流节点组件
- **Connection.vue** - 节点之间的连接组件

### 辅助组件

- **Toolbar.vue** - 工具栏组件，提供添加节点、缩放等功能
- **PropertiesPanel.vue** - 属性面板，用于编辑节点或连接的属性
- **TestPanel.vue** - 测试面板，用于测试工作流执行
- **HistoryPanel.vue** - 历史面板，显示工作流执行历史

## 使用方式

在视图中引入Editor组件：

```vue
<script setup lang="ts">
import Editor from '../workflow/components/Editor.vue'
</script>

<template>
  <Editor />
</template>
```

## 未来优化方向

1. 进一步拆分大型组件
2. 添加更多单元测试
3. 提高组件的可配置性
4. 优化性能，特别是对大型工作流的处理
5. 增强工作流验证和错误处理 
# 工作流编辑器开发规范

## 1. 系统架构

### 1.1 核心模块
- **工作流状态管理** (原 workflow-state.js)
  - 工作流配置数据结构
  - 节点和连接的状态管理
  - 撤销/重做功能

- **画布管理** (原 canvas-manager.js)
  - 拖拽功能
  - 缩放和平移
  - 网格对齐
  - 右键菜单

- **节点管理** (原 node-manager.js)
  - 节点创建和编辑
  - 节点类型：START、FUNCTION、CONDITION、END
  - 节点属性配置
  - 节点验证

- **连接管理** (原 connection-manager.js)
  - 节点间连接的创建和删除
  - 连接线绘制
  - 连接点交互

### 1.2 UI 组件结构
```
WorkflowEditor
├── EditorHeader
│   ├── NavigationBack
│   ├── WorkflowInfo
│   └── ActionButtons
├── WorkflowCanvas
│   ├── NodeComponent
│   │   ├── NodeHeader
│   │   ├── NodeContent
│   │   └── ConnectionPoints
│   └── ConnectionLines
├── ContextMenu
└── NodeEditorPanel
    ├── BasicInfo
    ├── Parameters
    └── Connections
```

## 2. 功能规范

### 2.1 工作流基本操作
- 创建新工作流
- 保存工作流
- 加载已有工作流
- 删除工作流
- 测试运行工作流

### 2.2 节点操作
- 通过右键菜单添加节点
- 拖拽移动节点
- 编辑节点属性
- 删除节点
- 复制/粘贴节点

### 2.3 连接操作
- 通过连接点创建连接
- 条件节点多分支处理
- 删除连接
- 连接验证（防止循环）

### 2.4 画布操作
- 拖拽平移画布
- 缩放画布
- 网格对齐
- 自动布局

## 3. 数据结构

### 3.1 工作流数据模型
```typescript
interface Workflow {
  id: string;
  name: string;
  description: string;
  nodes: Node[];
  connections: Connection[];
}

interface Node {
  id: string;
  type: 'START' | 'FUNCTION' | 'CONDITION' | 'END';
  name: string;
  position: { x: number; y: number };
  parameters: Record<string, any>;
}

interface Connection {
  id: string;
  sourceNodeId: string;
  targetNodeId: string;
  condition?: string; // 用于条件节点
}
```

### 3.2 API 接口
- GET /api/workflows - 获取工作流列表
- GET /api/workflows/{id} - 获取单个工作流
- POST /api/workflows - 创建工作流
- PUT /api/workflows/{id} - 更新工作流
- DELETE /api/workflows/{id} - 删除工作流
- POST /api/workflows/{id}/test - 测试运行工作流

## 4. 技术栈

### 4.1 核心依赖
- Vue 3 + TypeScript
- Vue Router
- Pinia (状态管理)

### 4.2 UI 组件
- 自定义基础组件，不依赖第三方 UI 库
- 使用 CSS Variables 实现主题定制
- Flexbox 和 Grid 布局
- canvas 用于连接线绘制

## 5. 注意事项

### 5.1 性能优化
- 使用 Vue 3 的 `<script setup>` 语法
- 大量节点时使用虚拟滚动
- 连接线重绘优化
- 状态更新批处理

### 5.2 代码规范
- 使用 TypeScript 强类型
- 组件采用 Composition API
- 遵循 Vue 3 最佳实践
- 模块化和可测试性


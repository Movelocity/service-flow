
# 工作流结构更新说明文档

## 1. 核心变更概述

### 1.1 工作流级别变更
- 新增工作流级别的输入输出定义
- 工具定义从节点移至工作流级别
- 全局变量现在为只读模式

### 1.2 节点级别变更
- 简化节点结构，移除冗余的输入输出定义
- 新增 context 机制用于管理函数节点的输出
- 工具引用方式改为仅通过 toolName 引用

## 2. 数据结构变更

### 2.1 工作流（Workflow）结构
```typescript
interface Workflow {
  id: string;
  name: string;
  description: string;
  inputs: { [key: string]: any };        // 新增：工作流级别输入
  outputs: { [key: string]: any };       // 新增：工作流级别输出
  tools: { [name: string]: ToolDefinition }; // 变更：工具定义移至工作流级别
  globalVariables: { [key: string]: any };   // 变更：现在为只读
  nodes: WorkflowNode[];
  startNodeId: string;
  isActive: boolean;
}
```

### 2.2 节点（WorkflowNode）结构
```typescript
interface WorkflowNode {
  id: string;
  name: string;
  description: string;
  type: NodeType;
  nextNodes: { [condition: string]: string }; // condition可以是'default'/'true'/'false'
  position: { x: number, y: number };
  toolName?: string;                      // 仅用于 FUNCTION 类型节点
  context: { [key: string]: any };        // 新增：存储函数节点的输出
}
```

## 3. 前端需要更新的内容

### 3.1 工作流编辑器
1. 工作流属性面板新增：
   - 工作流输入配置区域
   - 工作流输出配置区域
   - 工具列表管理区域

2. 节点属性面板简化：
   - 移除输入输出配置
   - 对于函数节点，仅保留工具选择
   - 新增 context 查看区域（用于调试）

### 3.2 变量引用方式
- 全局变量引用格式：`global:变量名`
- 节点输出引用格式：`节点名:输出参数名`
- 注意：节点名和参数名中不允许包含冒号

### 3.3 数据流向
```
工作流输入 -> 全局变量 -> 节点执行 -> 节点context -> 工作流输出
```

## 4. API 调整

### 4.1 工作流保存接口
- 需要包含完整的工具定义列表
- 需要包含工作流级别的输入输出定义

```typescript
// 保存工作流请求示例
{
  "id": "workflow1",
  "name": "示例工作流",
  "inputs": {
    "inputVar1": { "type": "string", "description": "输入参数1" }
  },
  "outputs": {
    "outputVar1": { "type": "number", "description": "输出参数1" }
  },
  "tools": {
    "toolName1": {
      "name": "toolName1",
      "description": "工具1描述",
      "inputs": {...},
      "outputs": {...}
    }
  },
  "nodes": [...]
}
```

### 4.2 工作流执行接口
- 启动工作流时需要提供完整的输入参数
- 执行完成后可获取完整的输出参数

```typescript
// 启动工作流
POST /api/workflows/{workflowId}/execute
{
  "inputs": {
    "inputVar1": "value1"
  }
}

// 获取工作流结果
GET /api/workflows/{workflowId}/executions/{executionId}/result
{
  "outputs": {
    "outputVar1": 42
  }
}
```

## 5. 迁移指南

### 5.1 数据迁移
1. 将节点中的工具定义迁移到工作流级别
2. 根据节点的输入输出定义，整理工作流级别的输入输出
3. 清理节点中冗余的输入输出定义

### 5.2 前端代码迁移
1. 更新工作流和节点的类型定义
2. 重构工作流编辑器的属性面板
3. 更新变量引用和工具选择的实现方式
4. 添加 context 相关的调试功能

### 5.3 注意事项
- 保持节点名称的唯一性
- 确保节点名称中不包含冒号
- 注意全局变量的只读特性
- 工具定义的变更需要同步更新前后端

## 6. 后续优化方向
- 支持工作流模板
- 增强全局变量的管理机制
- 提供更丰富的调试工具
- 优化工作流执行性能

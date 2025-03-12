# Workflow Controller API 文档

本文档描述了 `WorkflowController` 提供的用于管理工作流及其执行的 REST API 端点。

## 基础 URL
所有端点都以 `/api/workflows` 为前缀

## 端点

### 1. 创建工作流
在系统中创建一个新的工作流。

- **URL**: `/api/workflows`
- **方法**: `POST`
- **请求体**: 工作流对象
```jsonc
{
    "id": "string" (可选),
    // 工作流定义
}
```
- **响应**: 
  - 成功 (200): 返回带有生成的 ID 的创建的工作流
  - 错误 (500): 如果保存操作失败，则返回错误信息

### 2. 更新工作流
通过 ID 更新现有的工作流。

- **URL**: `/api/workflows/{workflowId}`
- **方法**: `PUT`
- **路径参数**:
  - `workflowId`: 要更新的工作流的 ID
- **请求体**: 更新后的工作流对象
- **响应**:
  - 成功 (200): 返回更新后的工作流
  - 错误 (500): 如果更新操作失败，则返回错误信息

### 3. 获取工作流
通过 ID 检索特定的工作流。

- **URL**: `/api/workflows/{workflowId}`
- **方法**: `GET`
- **路径参数**:
  - `workflowId`: 要检索的工作流的 ID
- **响应**:
  - 成功 (200): 返回请求的工作流
  - 未找到 (404): 如果工作流不存在

### 4. 列出工作流
检索所有工作流的列表。

- **URL**: `/api/workflows`
- **方法**: `GET`
- **响应**:
  - 成功 (200): 返回工作流 ID 的数组

### 5. 删除工作流
通过 ID 删除特定的工作流。

- **URL**: `/api/workflows/{workflowId}`
- **方法**: `DELETE`
- **路径参数**:
  - `workflowId`: 要删除的工作流的 ID
- **响应**:
  - 成功 (200): 返回 `{"success": true/false}`

### 6. 执行工作流
启动工作流的执行。

- **URL**: `/api/workflows/{workflowId}/execute`
- **方法**: `POST`
- **路径参数**:
  - `workflowId`: 要执行的工作流的 ID
- **请求体**: (可选)
```jsonc
{
    "key": "value" // 工作流执行的输入变量
}
```
- **响应**:
  - 成功 (200): 返回执行 ID `{"executionId": "string"}`
  - 错误 (500): 如果执行失败，则返回错误信息

### 7. 获取执行状态
检索工作流执行的状态。

- **URL**: `/api/workflows/{workflowId}/executions/{executionId}`
- **方法**: `GET`
- **路径参数**:
  - `workflowId`: 工作流的 ID
  - `executionId`: 执行的 ID
- **响应**:
  - 成功 (200): 返回状态 `{"status": "string"}`
  - 错误 (500): 如果状态检索失败，则返回错误信息

### 8. 调试工作流
以调试模式启动工作流的执行，返回 Server-Sent Events 流，包含节点执行的实时信息。

- **URL**: `/api/workflows/{workflowId}/debug`
- **方法**: `POST`
- **路径参数**:
  - `workflowId`: 要执行的工作流的 ID
- **请求体**: (可选)
```jsonc
{
    "key": "value" // 工作流执行的输入变量
}
```
- **响应**:
  - 成功: 返回 EventStream，每个事件包含以下格式的数据:
```jsonc
{
    "executionId": "string",
    "nodeId": "string",
    "nodeName": "string",
    "nodeType": "string",
    "eventType": "ENTER" | "COMPLETE",
    "nodeContext": {
        // 节点上下文数据
    },
    "globalVariables": {
        // 全局变量
    },
    "timestamp": "string",
    "duration": number // 仅在 COMPLETE 事件中存在
}
```
  - 错误 (500): 如果执行启动失败，则返回错误信息

## 错误响应
所有端点可能返回以下错误响应格式：
```json
{
    "error": "错误信息描述"
}
```
# 工作流校验指南

## 简介

工作流校验器是一个用于检查工作流定义正确性的工具。它可以帮助你在执行工作流之前发现潜在的问题，确保工作流能够正常运行。

## 主要功能

工作流校验器可以检查以下几个方面：

1. **函数节点参数检查**
   - 检查必填参数是否都已提供
   - 检查参数类型是否匹配
   - 验证引用的父节点是否存在

2. **条件节点检查**
   - 检查条件表达式中使用的变量是否存在
   - 检查条件分支的操作数类型是否兼容
   - 验证所有条件分支是否都有对应的后续节点

3. **节点连接检查**
   - 检查节点间的连接是否完整
   - 检查是否存在悬空的节点（非结束节点没有后续节点）
   - 检查引用的节点ID是否存在

## API 使用说明

### 接口信息

- **URL**: `/api/workflow/validation/validate`
- **方法**: POST
- **Content-Type**: application/json

### 请求参数

1. 请求体 (JSON格式):
   ```jsonc
   {
     "id": "flow_123",
     "name": "示例工作流",
     "description": "这是一个示例工作流",
     "startNodeId": "start",
     "nodes": [...],
     "inputs": {...},
     "outputs": {...},
     "globalVariables": {...}
   }
   ```

2. URL参数（可选）:
   - `startNodeId`: 指定开始验证的节点ID。如果不提供，将使用工作流定义中的默认起始节点。

### 响应格式

```jsonc
{
  "valid": true|false,
  "errors": [
    "错误信息1",
    "错误信息2",
    ...
  ]
}
```

### 示例

#### 请求示例

```http
POST /api/workflow/validation/validate HTTP/1.1
Content-Type: application/json

{
  "id": "flow_example",
  "name": "售后服务流程",
  "startNodeId": "start",
  "nodes": [
    {
      "id": "start",
      "name": "开始",
      "type": "START",
      "nextNodes": {
        "default": "node1"
      }
    },
    {
      "id": "node1",
      "name": "检查订单",
      "type": "FUNCTION",
      "toolName": "order_check",
      "inputMap": {
        "orderId": {
          "type": "STRING",
          "value": "123"
        }
      }
    }
  ]
}
```

#### 响应示例

成功情况：
```json
{
  "valid": true,
  "errors": []
}
```

存在错误时：
```json
{
  "valid": false,
  "errors": [
    "Function node '检查订单' (ID: node1) is missing required parameter: customerName",
    "Node '检查订单' (ID: node1) has no next nodes but is not an END node"
  ]
}
```

## 常见错误说明

1. **缺少必填参数**
   ```
   Function node '{节点名称}' (ID: {节点ID}) is missing required parameter: {参数名}
   ```
   解决方法：检查函数节点的 inputMap 是否包含所有必需的参数。

2. **参数类型不匹配**
   ```
   Function node '{节点名称}' (ID: {节点ID}) parameter '{参数名}' has incompatible type. Expected: {期望类型}, Found: {实际类型}
   ```
   解决方法：确保参数值的类型与工具定义中要求的类型一致。

3. **条件分支不完整**
   ```
   Condition node '{节点名称}' (ID: {节点ID}) is missing branch for: {分支名称}
   ```
   解决方法：为条件节点的每个分支（包括else分支）都指定后续节点。

4. **引用不存在的节点**
   ```
   Node '{节点名称}' (ID: {节点ID}) references non-existent next node: {目标节点ID}
   ```
   解决方法：检查节点连接是否正确，确保所有引用的节点ID都存在。

## 最佳实践

1. **参数类型检查**
   - 在设置函数节点参数时，注意检查类型匹配
   - 数字类型（NUMBER）和整数类型（INTEGER）是兼容的
   - 字符串类型（STRING）需要严格匹配

2. **条件节点设置**
   - 确保每个条件分支都有对应的后续节点
   - 不要忘记设置 else 分支
   - 检查条件中使用的变量是否已经在之前的节点中定义

3. **节点连接**
   - 除了结束节点外，其他节点都应该有后续节点
   - 检查节点ID的引用是否正确
   - 避免创建循环引用

4. **变量引用**
   - 使用其他节点的输出时，确保引用的节点在工作流中存在
   - 注意区分全局变量（parent="global"）和节点输出变量

## 注意事项

1. 校验器不会执行实际的工作流，只会进行静态检查
2. 即使校验通过，也不能保证工作流在运行时不会出现其他问题
3. 建议在开发和修改工作流时经常进行校验，及早发现问题
4. 对于复杂的工作流，建议分段开发和测试，确保每个部分都正确后再组合
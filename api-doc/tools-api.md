# 工具控制器 API 文档

本文档描述了工具框架提供的用于管理和执行工具的 REST API 端点。

## 基础 URL
所有端点都以 `/api/tools` 为前缀

## 端点

### 1. 列出所有工具
检索系统中所有可用工具的列表。

- **URL**: `/api/tools`
- **方法**: `GET`
- **响应**: 
  - 成功 (200): 返回工具数组
```json
[
  {
    "name": "string",
    "description": "string"
  }
]
```

### 2. 获取工具详情
检索特定工具的详细信息。

- **URL**: `/api/tools/{toolName}`
- **方法**: `GET`
- **路径参数**:
  - `toolName`: 要检索的工具名称
- **响应**:
  - 成功 (200): 返回工具详情
```json
{
  "name": "string",
  "description": "string",
  "inputs": {
    "fieldName": {
      "name": "string",
      "description": "string",
      "type": "STRING|NUMBER|BOOLEAN|ARRAY|OBJECT|DATE",
      "required": true,
      "defaultValue": null,
      "constraints": null
    }
  },
  "outputs": {
    "fieldName": {
      "name": "string",
      "description": "string",
      "type": "STRING|NUMBER|BOOLEAN|ARRAY|OBJECT|DATE",
      "required": true
    }
  }
}
```
  - 未找到 (404): 如果工具不存在

### 3. 执行工具
使用提供的参数执行特定工具。

- **URL**: `/api/tools/{toolName}/execute`
- **方法**: `POST`
- **路径参数**:
  - `toolName`: 要执行的工具名称
- **请求体**: 工具特定参数
```json
{
    // 工具特定参数
}
```
- **响应**:
  - 成功 (200): 返回执行结果
```json
{
  "success": true,
  "data": {
    // 工具特定响应数据
  },
  "message": "操作成功"
}
```
  - 错误请求 (400): 如果参数无效
  - 未找到 (404): 如果工具不存在

## 错误处理

API 使用标准 HTTP 状态码：
- 200: 成功
- 400: 错误请求（参数无效）
- 404: 未找到（工具未找到）
- 500: 内部服务器错误

错误响应在响应体中包含错误代码和消息：
```json
{
  "success": false,
  "errorCode": "ERROR_CODE",
  "message": "错误描述"
}
```

## 示例用法

### 列出所有工具
```bash
curl -X GET http://localhost:8080/api/tools
```

### 获取工具详情
```bash
curl -X GET http://localhost:8080/api/tools/tool_name
```

### 执行工具
```bash
curl -X POST http://localhost:8080/api/tools/tool_name/execute \
  -H "Content-Type: application/json" \
  -d '{
    "param1": "value1",
    "param2": "value2"
  }'
```
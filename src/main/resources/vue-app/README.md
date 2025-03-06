# 工作流管理系统 - 前端

这是工作流管理系统的前端部分，使用Vue 3、TypeScript、Pinia和Bootstrap构建。

## 功能特性

- 工作流列表展示
- 创建、编辑、删除工作流
- 工作流执行和状态监控
- 响应式设计，适配不同设备

## 开发环境设置

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 项目结构

- `src/views/` - 页面组件
- `src/components/` - 可复用组件
- `src/stores/` - Pinia状态管理
- `src/services/` - API服务
- `src/router/` - 路由配置

## API接口

系统使用RESTful API与后端通信，主要接口包括：

- `GET /api/workflows` - 获取所有工作流ID
- `GET /api/workflows/:id` - 获取特定工作流详情
- `POST /api/workflows` - 创建新工作流
- `PUT /api/workflows/:id` - 更新工作流
- `DELETE /api/workflows/:id` - 删除工作流
- `POST /api/workflows/:id/execute` - 执行工作流
- `GET /api/workflows/:id/executions/:executionId` - 获取执行状态

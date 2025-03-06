# Tools Framework 使用指南

本框架提供了一个统一的工具注册和管理机制，使开发人员能够方便地创建和注册新的工具。

## 目录结构

```
cn.yafex.tools/
├── core/           # 核心接口和类
│   ├── ToolHandler.java       # 工具处理器接口
│   ├── ToolDefinition.java    # 工具定义类
│   ├── ToolResponse.java      # 响应封装类
│   └── ToolRegistry.java      # 工具注册中心
├── schema/         # 参数定义
│   ├── FieldType.java         # 字段类型枚举
│   └── FieldDefinition.java   # 字段定义类
├── exceptions/     # 异常处理
│   ├── ToolException.java     # 基础异常类
│   └── ValidationException.java# 参数验证异常
├── handlers/       # 具体工具实现
│   └── system/                # 系统工具示例
└── utils/          # 工具类
    └── CollectionUtils.java   # 集合工具类
```

## 创建新工具的步骤

1. **创建工具类**
   - 在适当的包下创建新的工具类
   - 实现 `ToolHandler` 接口
   - 示例：
   ```java
   public class MyNewTool implements ToolHandler {
       private final ToolDefinition definition;

       public MyNewTool() {
           // 定义输入参数
           Map<String, FieldDefinition> inputFields = new HashMap<>();
           inputFields.put("param1", new FieldDefinition(
               "param1",
               "Parameter description",
               FieldType.STRING,
               true,  // 是否必需
               null,  // 默认值
               null   // 约束条件
           ));

           // 定义输出参数
           Map<String, FieldDefinition> outputFields = new HashMap<>();
           outputFields.put("result", new FieldDefinition(
               "result",
               "Result description",
               FieldType.OBJECT,
               true,
               null,
               null
           ));

           // 创建工具定义
           this.definition = new ToolDefinition(
               "tool_name",           // 工具名称
               "Tool description",    // 工具描述
               inputFields,           // 输入参数定义
               outputFields,          // 输出参数定义
               CollectionUtils.listOf(ToolException.class)  // 可能的异常
           );
       }

       @Override
       public String getName() {
           return definition.getName();
       }

       @Override
       public ToolDefinition getDefinition() {
           return definition;
       }

       @Override
       @SuppressWarnings("unchecked")
       public <T> ToolResponse<T> execute(Map<String, Object> params) throws ToolException {
           // 1. 验证参数
           validateParams(params);

           try {
               // 2. 获取参数
               String param1 = (String) params.get("param1");

               // 3. 执行业务逻辑
               Map<String, Object> result = new HashMap<>();
               // ... 实现具体功能 ...

               // 4. 返回结果
               return (ToolResponse<T>) ToolResponse.success(
                   result,
                   "Operation successful"
               );

           } catch (Exception e) {
               throw new ToolException(
                   "Operation failed: " + e.getMessage(),
                   "ERROR_CODE"
               );
           }
       }
   }
   ```

2. **参数定义**
   - 使用 `FieldDefinition` 定义输入和输出参数
   - 支持的字段类型（`FieldType`）：
     * STRING: 字符串
     * NUMBER: 数字
     * BOOLEAN: 布尔值
     * ARRAY: 数组
     * OBJECT: 对象
     * DATE: 日期

3. **错误处理**
   - 使用 `ToolException` 处理业务异常
   - 使用 `ValidationException` 处理参数验证异常
   - 为每种错误定义唯一的错误代码

4. **注册工具**
   - 方式一：使用Spring自动注册（推荐）
   ```java
   @Component
   public class MyNewTool implements ToolHandler {
       // ... 工具实现 ...
   }
   ```

   - 方式二：手动注册
   ```java
   ToolRegistry.register(new MyNewTool());
   ```

## 使用示例

```java
// 1. 获取工具实例
ToolHandler tool = ToolRegistry.getHandler("tool_name");

// 2. 准备参数
Map<String, Object> params = new HashMap<>();
params.put("param1", "value1");

// 3. 执行工具
try {
    ToolResponse<?> response = tool.execute(params);
    if (response.isSuccess()) {
        // 处理成功响应
        Object result = response.getData();
    } else {
        // 处理错误响应
        String errorCode = response.getErrorCode();
        String message = response.getMessage();
    }
} catch (ToolException e) {
    // 处理异常
    String errorCode = e.getErrorCode();
    Map<String, Object> details = e.getDetails();
}
```

## 最佳实践

1. **参数验证**
   - 为必需参数设置 `required = true`
   - 使用约束条件限制参数值范围
   - 在 `execute` 方法中添加额外的业务验证

2. **错误处理**
   - 使用有意义的错误代码
   - 在异常中包含详细信息
   - 适当使用异常层次结构

3. **响应格式**
   - 使用 `ToolResponse` 封装所有响应
   - 包含必要的元数据
   - 提供清晰的成功/失败标识

4. **代码组织**
   - 按功能类型组织工具类
   - 使用适当的包结构
   - 保持单一职责原则

## 注意事项

1. 工具名称必须唯一
2. 参数验证要充分
3. 错误处理要完善
4. 响应格式要统一
5. 文档要及时更新
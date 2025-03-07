关于工作流中的变量
传递变量：前置节点可以命名自己的输出变量，后置节点将在 context 看到所有前置节点的输出变量并继续传递这些变量，这些变量的类型为 {parent: string, type: string, value: any}

开始节点
一个工作流中只能有一个，可以指定工作流启动时接收的一组变量名称、类型、是否可选。可以当作全局变量使用

条件节点
可以选择上下文存在的变量
- 字符串类型变量的判断条件可以选`包含，不包含，开始是，结束是，是，不是，为空，不为空`，根据情况可以有第二个 operand
- 数字类型的判断条件可以是 `=, !=, >, <, >=, <=, 为空, 不为空`
- 布尔值不需要判断条件，本身包含语义

工具节点
选择具体工具时，可以知道工具的输入和输出参数
```json
{
  "name": "string",
  "description": "string",
  "inputFields": {
    "fieldName": {
      "name": "string",
      "description": "string",
      "type": "STRING|NUMBER|BOOLEAN",
      "required": true,
      "defaultValue": null,
      "constraints": null
    }
  },
  "outputFields": {
    "fieldName": {
      "name": "string",
      "description": "string",
      "type": "STRING|NUMBER|BOOLEAN",
      "required": true
    }
  }
}
```
通过 `{{paramParent:paramName}}` 在字符串中引用变量，也可以不适用上下文参数，直接设定传给工具的默认值
输出变量可以有多个，可以重命名，在后续节点中被视为上下文变量

前端不负责执行工作流，配置顺利保存到后端后，可由前端发起工作流调用，并传递起始参数
package cn.yafex.tools.core;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ToolField;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.exceptions.ValidationException;
import cn.yafex.tools.schema.FieldDefinition;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 所有工具实现必须实现的接口
 */
public interface ToolHandler {
    /**
     * 获取工具的唯一名称
     */
    default String getName() {
        Tool annotation = getClass().getAnnotation(Tool.class);
        if (annotation == null) {
            throw new IllegalStateException("Tool annotation not found on class: " + getClass().getName());
        }
        return annotation.name();
    }

    /**
     * 获取工具的定义，包括参数和返回类型
     */
    default ToolDefinition getDefinition() {
        Tool annotation = getClass().getAnnotation(Tool.class);
        if (annotation == null) {
            throw new IllegalStateException("Tool annotation not found on class: " + getClass().getName());
        }

        // Find execute method
        Method executeMethod = null;
        for (Method method : getClass().getMethods()) {
            if (method.getName().equals("execute")) {
                executeMethod = method;
                break;
            }
        }
        
        if (executeMethod == null) {
            throw new IllegalStateException("No execute method found");
        }
        
        // 从参数构建输入字段
        Map<String, FieldDefinition> inputFields = new HashMap<>();
        for (Parameter param : executeMethod.getParameters()) {
            ToolField field = param.getAnnotation(ToolField.class);
            if (field != null) {
                inputFields.put(field.name(), new FieldDefinition(
                    field.name(),
                    field.description(),
                    field.type(),
                    field.required(),
                    field.defaultValue(),
                    field.pattern()
                ));
            }
        }
        
        // 从方法构建输出字段
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        
        // 首先尝试获取可重复的注解
        ToolField[] fields = executeMethod.getAnnotationsByType(ToolField.class);
        if (fields.length > 0) {
            for (ToolField field : fields) {
                outputFields.put(field.name(), new FieldDefinition(
                    field.name(),
                    field.description(),
                    field.type(),
                    field.required(),
                    field.defaultValue(),
                    field.pattern()
                ));
            }
        }

        return new ToolDefinition(
            annotation.name(),
            annotation.description(),
            inputFields,
            outputFields,
            null
        );
    }

    /**
     * 使用给定的参数执行工具
     * @param params 参数名称到值的映射
     * @return 包含执行结果的响应
     * @throws ToolException 如果执行失败
     */
    <T> ToolResponse<T> execute(Map<String, Object> params) throws ToolException;

    /**
     * 验证输入参数是否符合工具的定义
     * @param params 参数名称到值的映射
     * @throws ValidationException 如果验证失败
     */
    default void validateParams(Map<String, Object> params) throws ValidationException {
        ToolDefinition definition = getDefinition();
        Map<String, FieldDefinition> inputFields = definition.getInputs();

        // 检查必填字段
        for (Map.Entry<String, FieldDefinition> entry : inputFields.entrySet()) {
            String fieldName = entry.getKey();
            FieldDefinition field = entry.getValue();

            if (field.isRequired() && !params.containsKey(fieldName)) {
                throw new ValidationException("Missing required field: " + fieldName);
            }
        }

        // 可以在类中实现或重写此方法
    }
} 
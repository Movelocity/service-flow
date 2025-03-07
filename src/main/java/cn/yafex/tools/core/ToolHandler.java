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
 * Interface that all tool implementations must implement
 */
public interface ToolHandler {
    /**
     * Get the unique name of the tool
     */
    default String getName() {
        Tool annotation = getClass().getAnnotation(Tool.class);
        if (annotation == null) {
            throw new IllegalStateException("Tool annotation not found on class: " + getClass().getName());
        }
        return annotation.name();
    }

    /**
     * Get the tool's definition including parameters and return type
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
        
        // Build input fields from parameters
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
        
        // Build output fields from method
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        ToolField returnField = executeMethod.getAnnotation(ToolField.class);
        if (returnField != null) {
            outputFields.put(returnField.name(), new FieldDefinition(
                returnField.name(),
                returnField.description(),
                returnField.type(),
                returnField.required(),
                returnField.defaultValue(),
                returnField.pattern()
            ));
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
     * Execute the tool with the given parameters
     * @param params Map of parameter names to values
     * @return Response containing the execution result
     * @throws ToolException if execution fails
     */
    <T> ToolResponse<T> execute(Map<String, Object> params) throws ToolException;

    /**
     * Validate the input parameters against the tool's definition
     * @param params Map of parameter names to values
     * @throws ValidationException if validation fails
     */
    default void validateParams(Map<String, Object> params) throws ValidationException {
        ToolDefinition definition = getDefinition();
        Map<String, FieldDefinition> inputFields = definition.getInputFields();

        // Check required fields
        for (Map.Entry<String, FieldDefinition> entry : inputFields.entrySet()) {
            String fieldName = entry.getKey();
            FieldDefinition field = entry.getValue();

            if (field.isRequired() && !params.containsKey(fieldName)) {
                throw new ValidationException("Missing required field: " + fieldName);
            }
        }

        // Additional validation can be implemented here or in concrete classes
    }
} 
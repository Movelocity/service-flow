package cn.yafex.tools.core;

import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.exceptions.ValidationException;
import cn.yafex.tools.schema.FieldDefinition;
import java.util.Map;

/**
 * Interface that all tool implementations must implement
 */
public interface ToolHandler {
    /**
     * Get the unique name of the tool
     */
    String getName();

    /**
     * Get the tool's definition including parameters and return type
     */
    ToolDefinition getDefinition();

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
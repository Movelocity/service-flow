package cn.yafex.tools.handlers.text;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ToolField;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;
import java.util.HashMap;
import java.util.Map;

@Tool(
    name = "text_process",
    description = "Process text with basic operations"
)
public class TextProcessTool implements ToolHandler {
    @Override
    @ToolField(name = "length", description = "Length of the input text", type = FieldType.NUMBER)
    @ToolField(name = "words", description = "Number of words in the text", type = FieldType.NUMBER)
    @ToolField(name = "uppercase", description = "Text converted to uppercase", type = FieldType.STRING)
    @ToolField(name = "lowercase", description = "Text converted to lowercase", type = FieldType.STRING)
    @ToolField(name = "trimmed", description = "Text with leading and trailing whitespace removed", type = FieldType.STRING)
    public <T> ToolResponse<T> execute(
        @ToolField(
            name = "text",
            description = "Input text to process",
            type = FieldType.STRING,
            required = true
        ) Map<String, Object> params
    ) throws ToolException {
        String text = (String) params.get("text");
        
        if (text == null || text.isEmpty()) {
			@SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.error("Input text cannot be empty", "INVALID_INPUT");
            return response;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("length", text.length());
        result.put("words", text.split("\\s+").length);
        result.put("uppercase", text.toUpperCase());
        result.put("lowercase", text.toLowerCase());
        result.put("trimmed", text.trim());

		@SuppressWarnings("unchecked")
        ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(result);
        return response;
    }
} 
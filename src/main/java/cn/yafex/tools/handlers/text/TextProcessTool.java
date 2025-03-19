package cn.yafex.tools.handlers.text;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.InputVar;
import cn.yafex.tools.annotations.ReturnVal;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;
import java.util.HashMap;
import java.util.Map;

@Tool(
    name = "text_process",
    description = "处理文本的基本操作"
)
public class TextProcessTool implements ToolHandler {
    @Override
    @ReturnVal(name = "length", description = "输入文本的长度", type = FieldType.NUMBER)
    @ReturnVal(name = "words", description = "文本中的单词数量", type = FieldType.NUMBER)
    @ReturnVal(name = "uppercase", description = "转换为大写", type = FieldType.STRING)
    @ReturnVal(name = "lowercase", description = "转换为小写", type = FieldType.STRING)
    @ReturnVal(name = "trimmed", description = "去除前后空白", type = FieldType.STRING)
    public <T> ToolResponse<T> execute(
        @InputVar(name = "text", description = "输入的文本", type = FieldType.STRING, required = true)
        @InputVar(name = "truncate", description = "截断长度", type = FieldType.NUMBER, required = false, defaultValue = "100") 
		Map<String, Object> params
    ) throws ToolException {
        String text = (String) params.get("text");
        Integer truncate = (Integer) params.get("truncate");

        if (text == null || text.isEmpty()) {
			@SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.error("Input text cannot be empty", "INVALID_INPUT");
            return response;
        }

		if (truncate != null) {
			text = text.substring(0, truncate);
		}

        Map<String, Object> result = new HashMap<>();
        result.put("length", text.length());
        result.put("words", text.split("\\s+").length);
        result.put("uppercase", text.toUpperCase());
        result.put("lowercase", text.toLowerCase());
        result.put("trimmed", text.trim());

		
        ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(result);
        return response;
    }
} 
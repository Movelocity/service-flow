package cn.yafex.tools.handlers.system;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ToolField;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Tool(
    name = "file_info",
    description = "Get detailed information about a file"
)
public class FileInfoTool implements ToolHandler {
    @Override
    @ToolField(
        name = "info",
        description = "File information",
        type = FieldType.OBJECT
    )
    public <T> ToolResponse<T> execute(
        @ToolField(
            name = "path",
            description = "Path to the file",
            type = FieldType.STRING,
            required = true
        ) Map<String, Object> params
    ) throws ToolException {
        String path = (String) params.get("path");
        File file = new File(path);
        
        if (!file.exists()) {
            return ToolResponse.error("File not found: " + path, "FILE_NOT_FOUND");
        }

        Map<String, Object> info = new HashMap<>();
        info.put("name", file.getName());
        info.put("absolutePath", file.getAbsolutePath());
        info.put("size", file.length());
        info.put("isDirectory", file.isDirectory());
        info.put("lastModified", file.lastModified());
        info.put("canRead", file.canRead());
        info.put("canWrite", file.canWrite());

        @SuppressWarnings("unchecked")
        ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(info);
        return response;
    }
} 
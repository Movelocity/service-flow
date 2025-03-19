package cn.yafex.tools.handlers.system;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.InputVar;
import cn.yafex.tools.annotations.ReturnVal;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;
import java.io.File;
import java.util.*;

/**
 * Tool for listing directory contents
 */
@Tool(
    name = "list_directory",
    description = "列出目录内容"
)
public class ListDirectoryTool implements ToolHandler {
    @Override
    @ReturnVal(
        name = "entries",
        description = "目录条目列表",
        type = FieldType.ARRAY,
        required = true
    )
    public <T> ToolResponse<T> execute(
        @InputVar(
            name = "path",
            description = "要列出的目录路径",
            type = FieldType.STRING,
            required = true,
            defaultValue = "."
        ) Map<String, Object> params
    ) throws ToolException {
        validateParams(params);
        String path = (String) params.get("path");
        File dir = new File(path);

        if (!dir.exists() || !dir.isDirectory()) {
            @SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.error(
                "目录不存在: " + path, "DIRECTORY_NOT_FOUND"
            );
            return response;
        }

        try {
            List<Map<String, Object>> entries = new ArrayList<>();
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("name", file.getName());
                    entry.put("path", file.getAbsolutePath());
                    entry.put("size", file.length());
                    entry.put("isDirectory", file.isDirectory());
                    entry.put("lastModified", new Date(file.lastModified()));
                    entry.put("isHidden", file.isHidden());
                    entries.add(entry);
                }
            }
            
            // Now we can directly return the entries list, the system will automatically
            // wrap it in a map with key "items" based on our ToolResponse.ensureMapResponse method
            @SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(
                entries,
                "成功列出目录内容"
            );
            return response;

        } catch (Exception e) {
            throw new ToolException(
                "列出目录失败: " + e.getMessage(), "LIST_DIRECTORY_ERROR"
            );
        }
    }
} 
package cn.yafex.tools.handlers.system;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ToolField;
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
    description = "List contents of a directory with detailed information"
)
public class ListDirectoryTool implements ToolHandler {
    @Override
    @ToolField(
        name = "entries",
        description = "List of directory entries with details",
        type = FieldType.ARRAY,
        required = true
    )
    public <T> ToolResponse<T> execute(
        @ToolField(
            name = "path",
            description = "Directory path to list",
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
                "Directory does not exist: " + path,
                "DIRECTORY_NOT_FOUND"
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
                    entry.put("canRead", file.canRead());
                    entry.put("canWrite", file.canWrite());
                    entry.put("canExecute", file.canExecute());
                    entry.put("isHidden", file.isHidden());
                    entries.add(entry);
                }
            }
			@SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(
                entries,
                "Successfully listed directory contents"
            );
			return response;

        } catch (Exception e) {
            throw new ToolException(
                "Failed to list directory: " + e.getMessage(),
                "LIST_DIRECTORY_ERROR"
            );
        }
    }
} 
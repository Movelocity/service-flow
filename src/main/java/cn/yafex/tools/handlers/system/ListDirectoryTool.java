package cn.yafex.tools.handlers.system;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldDefinition;
import cn.yafex.tools.schema.FieldType;
import cn.yafex.tools.utils.CollectionUtils;

import java.io.File;
import java.util.*;

/**
 * Tool for listing directory contents
 */
public class ListDirectoryTool implements ToolHandler {
    private final ToolDefinition definition;

    public ListDirectoryTool() {
        // Define input parameters
        Map<String, FieldDefinition> inputFields = new HashMap<>();
        inputFields.put("path", new FieldDefinition(
            "path",
            "Directory path to list",
            FieldType.STRING,
            true,
            ".",
            null
        ));

        // Define output parameters
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        outputFields.put("files", new FieldDefinition(
            "files",
            "List of file and directory entries",
            FieldType.ARRAY,
            true,
            null,
            null
        ));

        this.definition = new ToolDefinition(
            "list_directory",
            "List contents of a directory",
            inputFields,
            outputFields,
            CollectionUtils.listOf(ToolException.class)
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
        validateParams(params);

        try {
            String path = (String) params.get("path");
            File dir = new File(path);

            if (!dir.exists() || !dir.isDirectory()) {
                return (ToolResponse<T>) ToolResponse.error(
                    "Directory does not exist: " + path,
                    "DIRECTORY_NOT_FOUND"
                );
            }

            List<Map<String, Object>> fileList = new ArrayList<>();
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    fileInfo.put("path", file.getPath());
                    fileInfo.put("isDirectory", file.isDirectory());
                    fileInfo.put("size", file.length());
                    fileInfo.put("lastModified", new Date(file.lastModified()));
                    fileList.add(fileInfo);
                }
            }

            return (ToolResponse<T>) ToolResponse.success(
                fileList,
                "Successfully listed directory contents"
            );

        } catch (Exception e) {
            throw new ToolException(
                "Failed to list directory: " + e.getMessage(),
                "LIST_DIRECTORY_ERROR"
            );
        }
    }
} 
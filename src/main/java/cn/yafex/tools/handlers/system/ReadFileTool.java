package cn.yafex.tools.handlers.system;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldDefinition;
import cn.yafex.tools.schema.FieldType;
import cn.yafex.tools.utils.CollectionUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Tool for reading file contents
 */
public class ReadFileTool implements ToolHandler {
    private final ToolDefinition definition;

    public ReadFileTool() {
        // Define input parameters
        Map<String, FieldDefinition> inputFields = new HashMap<>();
        inputFields.put("path", new FieldDefinition(
            "path",
            "Path to the file to read",
            FieldType.STRING,
            true,
            null,
            null
        ));
        inputFields.put("maxLines", new FieldDefinition(
            "maxLines",
            "Maximum number of lines to read (0 for unlimited)",
            FieldType.NUMBER,
            false,
            1000,
            CollectionUtils.mapOf("minimum", 0)
        ));

        // Define output parameters
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        outputFields.put("content", new FieldDefinition(
            "content",
            "File contents",
            FieldType.STRING,
            true,
            null,
            null
        ));
        outputFields.put("metadata", new FieldDefinition(
            "metadata",
            "File metadata",
            FieldType.OBJECT,
            true,
            null,
            null
        ));

        this.definition = new ToolDefinition(
            "read_file",
            "Read contents of a file",
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
            int maxLines = params.containsKey("maxLines") ? 
                ((Number) params.get("maxLines")).intValue() : 1000;

            File file = new File(path);
            if (!file.exists() || !file.isFile()) {
				
                return (ToolResponse<T>) ToolResponse.error(
                    "File does not exist: " + path,
                    "FILE_NOT_FOUND"
                );
            }

            // Read file content
            List<String> lines = Files.readAllLines(file.toPath());
            if (maxLines > 0 && lines.size() > maxLines) {
                lines = lines.subList(0, maxLines);
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("content", String.join("\n", lines));
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("size", file.length());
            metadata.put("lastModified", new Date(file.lastModified()));
            metadata.put("totalLines", lines.size());
            metadata.put("truncated", maxLines > 0 && lines.size() >= maxLines);
            response.put("metadata", metadata);

            return (ToolResponse<T>) ToolResponse.success(
                response,
                "Successfully read file contents"
            );

        } catch (Exception e) {
            throw new ToolException(
                "Failed to read file: " + e.getMessage(),
                "READ_FILE_ERROR"
            );
        }
    }
} 
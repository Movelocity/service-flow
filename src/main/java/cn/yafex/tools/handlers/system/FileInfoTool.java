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
)// @Tool 注解可以声明一个工具的名称和描述，供工具管理器自动扫描并注册
public class FileInfoTool implements ToolHandler {

    @Override  // @ToolField 注解可以声明一个工具的返回值，多返回值表示返回的是 map
    @ToolField(name = "name", description = "Name of the file", type = FieldType.STRING)
    @ToolField(name = "absolutePath", description = "Absolute path of the file", type = FieldType.STRING)
    @ToolField(name = "size", description = "Size of the file in bytes", type = FieldType.NUMBER)
    @ToolField(name = "isDirectory", description = "Whether the file is a directory", type = FieldType.BOOLEAN)
    @ToolField(name = "lastModified", description = "Last modified timestamp of the file", type = FieldType.NUMBER)
    public <T> ToolResponse<T> execute(  // 工具的执行方法，返回值类型为 ToolResponse<T>，T 为泛型，表示返回值的类型
        @ToolField(  // @ToolField 注解可以声明一个工具的参数，多参数应有多个注解
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

        @SuppressWarnings("unchecked")
        ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(info);
        return response;
    }
} 
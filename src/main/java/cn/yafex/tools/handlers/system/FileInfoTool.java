package cn.yafex.tools.handlers.system;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.InputVar;
import cn.yafex.tools.annotations.ReturnVal;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Tool(
    name = "file_info",
    description = "获取文件详细信息"
)// @Tool 注解可以声明一个工具的名称和描述，供工具管理器自动扫描并注册
public class FileInfoTool implements ToolHandler {

    @Override  // @ReturnVal 注解可以声明一个工具的返回值，多返回值表示返回的是 map
    @ReturnVal(name = "name", description = "文件名", type = FieldType.STRING)
    @ReturnVal(name = "absolutePath", description = "文件绝对路径", type = FieldType.STRING)
    @ReturnVal(name = "size", description = "文件大小", type = FieldType.NUMBER)
    @ReturnVal(name = "isDirectory", description = "是否是目录", type = FieldType.BOOLEAN)
    @ReturnVal(name = "lastModified", description = "文件最后修改时间", type = FieldType.NUMBER)
    public <T> ToolResponse<T> execute(  // 工具的执行方法，返回值类型为 ToolResponse<T>，T 为泛型，表示返回值的类型
        @InputVar(  // @InputVar 注解可以声明一个工具的参数，多参数应有多个注解
            name = "path",
            description = "文件路径",
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
package cn.yafex.tools.controller;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolRegistry;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/** 工具相关 API */
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    /**
     * 列出所有可用工具
     * @return 工具名称和描述列表
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listTools() {
		System.out.println("listTools");
        List<Map<String, String>> tools = ToolRegistry.getAllHandlers().stream()
            .map(handler -> {
                Map<String, String> toolInfo = new HashMap<>();
                toolInfo.put("name", handler.getName());
                toolInfo.put("description", handler.getDefinition().getDescription());
                return toolInfo;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tools);
    }

    /**
     * 获取特定工具的详细信息
     * @param toolName 工具名称
     * @return 工具定义，包括输入/输出参数
     */
    @GetMapping("/{toolName}")
    public ResponseEntity<ToolDefinition> getToolDetails(@PathVariable String toolName) {
		System.out.println("getToolDetails: " + toolName);
        ToolHandler handler = ToolRegistry.getHandler(toolName);
        if (handler == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(handler.getDefinition());
    }

    /**
     * 提供参数并执行一个工具
     * @param toolName 要执行的工具名称
     * @param params 工具参数
     * @return 工具执行响应
     */
    @PostMapping("/{toolName}/execute")
    public ResponseEntity<ToolResponse<?>> executeTool(
            @PathVariable String toolName,
            @RequestBody Map<String, Object> params) throws ToolException {
		System.out.println("executeTool: " + toolName);
        ToolHandler handler = ToolRegistry.getHandler(toolName);
        if (handler == null) {
            return ResponseEntity.notFound().build();
        }
        ToolResponse<?> response = handler.execute(params);
        return ResponseEntity.ok(response);
    }
} 
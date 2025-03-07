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

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    /**
     * List all available tools
     * @return List of tool names and their descriptions
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
     * Get detailed information about a specific tool
     * @param toolName Name of the tool
     * @return Tool definition including input/output parameters
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
     * Execute a tool with the provided parameters
     * @param toolName Name of the tool to execute
     * @param params Tool parameters
     * @return Tool execution response
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
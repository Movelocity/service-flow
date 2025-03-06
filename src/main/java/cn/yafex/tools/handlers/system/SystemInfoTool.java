package cn.yafex.tools.handlers.system;

import cn.yafex.tools.core.ToolDefinition;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldDefinition;
import cn.yafex.tools.schema.FieldType;
import cn.yafex.tools.utils.CollectionUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

/**
 * Tool for retrieving system information
 */
public class SystemInfoTool implements ToolHandler {
    private final ToolDefinition definition;

    public SystemInfoTool() {
        // Define output parameters
        Map<String, FieldDefinition> outputFields = new HashMap<>();
        outputFields.put("system", new FieldDefinition(
            "system",
            "System information",
            FieldType.OBJECT,
            true,
            null,
            null
        ));

        this.definition = new ToolDefinition(
            "system_info",
            "Get system information",
            new HashMap<>(),  // No input parameters needed
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
        try {
            Map<String, Object> systemInfo = new HashMap<>();
            
            // Get OS information
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            systemInfo.put("osName", System.getProperty("os.name"));
            systemInfo.put("osVersion", System.getProperty("os.version"));
            systemInfo.put("osArch", System.getProperty("os.arch"));
            systemInfo.put("processors", os.getAvailableProcessors());
            
            // Get Java information
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("javaVendor", System.getProperty("java.vendor"));
            systemInfo.put("javaHome", System.getProperty("java.home"));
            
            // Get memory information
            Runtime runtime = Runtime.getRuntime();
            systemInfo.put("totalMemory", runtime.totalMemory());
            systemInfo.put("freeMemory", runtime.freeMemory());
            systemInfo.put("maxMemory", runtime.maxMemory());
            
            // Get user information
            systemInfo.put("userName", System.getProperty("user.name"));
            systemInfo.put("userHome", System.getProperty("user.home"));
            systemInfo.put("userDir", System.getProperty("user.dir"));
            
            // Get time information
            systemInfo.put("timeZone", TimeZone.getDefault().getID());
            systemInfo.put("currentTime", new Date());

            return (ToolResponse<T>) ToolResponse.success(
                systemInfo,
                "Successfully retrieved system information"
            );

        } catch (Exception e) {
            throw new ToolException(
                "Failed to get system information: " + e.getMessage(),
                "SYSTEM_INFO_ERROR"
            );
        }
    }
} 
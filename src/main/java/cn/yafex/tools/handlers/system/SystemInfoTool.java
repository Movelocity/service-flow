package cn.yafex.tools.handlers.system;

import cn.yafex.tools.annotations.Tool;
import cn.yafex.tools.annotations.ReturnVal;
import cn.yafex.tools.core.ToolHandler;
import cn.yafex.tools.core.ToolResponse;
import cn.yafex.tools.exceptions.ToolException;
import cn.yafex.tools.schema.FieldType;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

/**
 * Tool for retrieving system information
 */
@Tool(
    name = "system_info",
    description = "Get system information"
)
public class SystemInfoTool implements ToolHandler {
    @Override
    @ReturnVal(name = "osName", description = "Operating system name", type = FieldType.STRING)
    @ReturnVal(name = "osVersion", description = "Operating system version", type = FieldType.STRING)
    @ReturnVal(name = "osArch", description = "Operating system architecture", type = FieldType.STRING)
    @ReturnVal(name = "processors", description = "Number of available processors", type = FieldType.NUMBER)
    @ReturnVal(name = "javaVersion", description = "Java runtime version", type = FieldType.STRING)
    @ReturnVal(name = "javaVendor", description = "Java vendor name", type = FieldType.STRING)
    @ReturnVal(name = "javaHome", description = "Java installation directory", type = FieldType.STRING)
    @ReturnVal(name = "totalMemory", description = "Total memory available to JVM", type = FieldType.NUMBER)
    @ReturnVal(name = "freeMemory", description = "Free memory available to JVM", type = FieldType.NUMBER)
    @ReturnVal(name = "maxMemory", description = "Maximum memory JVM will attempt to use", type = FieldType.NUMBER)
    @ReturnVal(name = "userName", description = "Current user name", type = FieldType.STRING)
    @ReturnVal(name = "userHome", description = "User home directory", type = FieldType.STRING)
    @ReturnVal(name = "userDir", description = "User working directory", type = FieldType.STRING)
    @ReturnVal(name = "timeZone", description = "System default timezone ID", type = FieldType.STRING)
    @ReturnVal(name = "currentTime", description = "Current system time", type = FieldType.STRING)
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

			@SuppressWarnings("unchecked")
            ToolResponse<T> response = (ToolResponse<T>) ToolResponse.success(
                systemInfo,
                "Successfully retrieved system information"
            );
			return response;

        } catch (Exception e) {
            throw new ToolException(
                "Failed to get system information: " + e.getMessage(),
                "SYSTEM_INFO_ERROR"
            );
        }
    }
} 
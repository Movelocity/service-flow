package cn.yafex.tools.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Collections;
import org.springframework.stereotype.Service;

/**
 * Registry for managing tool registrations
 */
@Service
public class ToolRegistry {
    private static final Map<String, ToolHandler> handlers = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    /**
     * Initialize the registry by scanning for tools
     * @param basePackage The base package to scan for tools
     */
    public static synchronized void initialize(String basePackage) {
        if (!initialized) {
            ToolScanner.scanAndRegister(basePackage);
            initialized = true;
        }
    }

	public ToolRegistry() {
		initialize("cn.yafex.tools.handlers");
	}

    /**
     * Register a new tool handler
     * @param handler The tool handler to register
     * @throws IllegalArgumentException if a handler with the same name already exists
     */
    public static void register(ToolHandler handler) {
        if (handlers.containsKey(handler.getName())) {
            throw new IllegalArgumentException("工具已存在注册: " + handler.getName());
        }
        handlers.put(handler.getName(), handler);
    }

    /**
     * Get a tool handler by name
     * @param name The name of the tool
     * @return The tool handler, or null if not found
     */
    public static ToolHandler getHandler(String name) {
        return handlers.get(name);
    }

    /**
     * Get all registered tool handlers
     * @return Unmodifiable collection of all registered handlers
     */
    public static Collection<ToolHandler> getAllHandlers() {
        return Collections.unmodifiableCollection(handlers.values());
    }

    /**
     * Unregister a tool handler
     * @param name The name of the tool to unregister
     * @return The removed handler, or null if not found
     */
    public static ToolHandler unregister(String name) {
        return handlers.remove(name);
    }

    /**
     * Check if a tool handler is registered
     * @param name The name of the tool
     * @return true if registered, false otherwise
     */
    public static boolean isRegistered(String name) {
        return handlers.containsKey(name);
    }

    /**
     * Clear all registered handlers
     */
    public static void clear() {
        handlers.clear();
        initialized = false;
    }
} 
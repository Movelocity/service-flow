package cn.yafex.tools.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Collections;
import org.springframework.stereotype.Service;

/**
 * 用于管理工具注册的注册表
 */
@Service
public class ToolRegistry {
    private static final Map<String, ToolHandler> handlers = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    /**
     * 初始化注册表，通过扫描工具
     * @param basePackage 要扫描的基包
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
     * 注册一个新的工具 handler
     * @param handler 要注册的工具 handler
     * @throws IllegalArgumentException 如果存在同名的工具 handler
     */
    public static void register(ToolHandler handler) {
        if (handlers.containsKey(handler.getName())) {
            throw new IllegalArgumentException("工具已存在注册: " + handler.getName());
        }
        handlers.put(handler.getName(), handler);
    }

    /**
     * 通过名称获取工具 handler
     * @param name 工具名称
     * @return 工具 handler，如果未找到则返回 null
     */
    public static ToolHandler getHandler(String name) {
        return handlers.get(name);
    }

    /**
     * 获取所有注册的工具 handler
     * @return 不可修改的工具 handler 集合
     */
    public static Collection<ToolHandler> getAllHandlers() {
        return Collections.unmodifiableCollection(handlers.values());
    }

    /**
     * 注销一个工具 handler
     * @param name 要注销的工具名称
     * @return 被移除的 handler，如果未找到则返回 null
     */
    public static ToolHandler unregister(String name) {
        return handlers.remove(name);
    }

    /**
     * 检查一个工具 handler 是否已注册
     * @param name 工具名称
     * @return 如果已注册则返回 true，否则返回 false
     */
    public static boolean isRegistered(String name) {
        return handlers.containsKey(name);
    }

    /**
     * 清除所有注册的 handler
     */
    public static void clear() {
        handlers.clear();
        initialized = false;
    }
} 
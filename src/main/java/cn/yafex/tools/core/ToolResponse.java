package cn.yafex.tools.core;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

/**
 * 标准化的工具执行结果响应格式
 * @param <T> 工具返回的数据类型
 * 
 * <p>关于返回值类型</p>
 * <ul>
 *     <li>ToolResponse 的 data 字段可以包含任何类型的数据</li>
 *     <li>WorkflowManager 会调用 ensureMapResponse 方法确保返回数据是 Map&lt;String, Object&gt; 格式</li>
 *     <li>如果想直接返回列表等数据结构，可以直接使用。系统会自动将列表包装到 "items" 键下</li>
 *     <li>如果返回单值，系统会自动将其包装到 "value" 键下</li>
 *     <li>如果已经是 Map&lt;String, Object&gt;，则保持不变</li>
 * </ul>
 */
public class ToolResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private Map<String, Object> metadata;

    public ToolResponse(boolean success, T data, String message, String errorCode) {
        this(success, data, message, errorCode, new HashMap<>());
    }

    public ToolResponse(boolean success, T data, String message, String errorCode, Map<String, Object> metadata) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // 静态工厂方法用于常见响应
    public static <T> ToolResponse<T> success(T data) {
        return new ToolResponse<>(true, data, "Operation successful", null);
    }

    public static <T> ToolResponse<T> success(T data, String message) {
        return new ToolResponse<>(true, data, message, null);
    }

    public static <T> ToolResponse<T> error(String message, String errorCode) {
        return new ToolResponse<>(false, null, message, errorCode);
    }
    
    /**
     * 确保返回数据是一个Map<String, Object>格式
     * 对于已经是Map<String, Object>的数据，直接返回
     * 对于Collection类型的数据，将其包装在一个以"items"为key的map中
     * 对于其他类型的数据，将其包装在一个以"value"为key的map中
     * 
     * @param data 任意类型的数据
     * @return 包装成Map<String, Object>的数据
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ensureMapResponse(Object data) {
        if (data == null) {
            return new HashMap<>();
        }
        
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        } 
        
        Map<String, Object> result = new HashMap<>();
        if (data instanceof Collection) {
            result.put("items", data);
        } else {
            result.put("value", data);
        }
        
        return result;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
} 
package cn.yafex.tools.core;

import java.util.Map;
import java.util.HashMap;

/**
 * Standardized response format for tool execution results
 * @param <T> The type of data returned by the tool
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

    // Static factory methods for common responses
    public static <T> ToolResponse<T> success(T data) {
        return new ToolResponse<>(true, data, "Operation successful", null);
    }

    public static <T> ToolResponse<T> success(T data, String message) {
        return new ToolResponse<>(true, data, message, null);
    }

    public static <T> ToolResponse<T> error(String message, String errorCode) {
        return new ToolResponse<>(false, null, message, errorCode);
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
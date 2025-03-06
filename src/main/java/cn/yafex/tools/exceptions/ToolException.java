package cn.yafex.tools.exceptions;

import java.util.Map;
import java.util.HashMap;

/**
 * Base exception class for all tool-related errors
 */
public class ToolException extends Exception {
    private String errorCode;
    private Map<String, Object> details;

    public ToolException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }

    public ToolException(String message, String errorCode, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details != null ? details : new HashMap<>();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
} 
package cn.yafex.tools.exceptions;

import java.util.List;
import java.util.ArrayList;

/**
 * Exception thrown when tool parameter validation fails
 */
public class ValidationException extends ToolException {
    private List<String> validationErrors;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = new ArrayList<>();
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void addValidationError(String error) {
        this.validationErrors.add(error);
    }
} 
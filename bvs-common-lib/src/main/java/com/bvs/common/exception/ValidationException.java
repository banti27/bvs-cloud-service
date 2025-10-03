package com.bvs.common.exception;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends BvsException {
    
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
    
    public ValidationException(String fieldName, String message) {
        super("VALIDATION_ERROR",
              String.format("Validation failed for field '%s': %s", fieldName, message));
    }
}

package com.bvs.common.exception;

/**
 * Exception thrown when a resource already exists
 */
public class ResourceAlreadyExistsException extends BvsException {
    
    public ResourceAlreadyExistsException(String message) {
        super("RESOURCE_ALREADY_EXISTS", message);
    }
    
    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super("RESOURCE_ALREADY_EXISTS",
              String.format("%s already exists with %s: %s", resourceName, fieldName, fieldValue));
    }
}

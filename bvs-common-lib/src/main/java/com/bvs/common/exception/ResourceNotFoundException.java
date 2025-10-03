package com.bvs.common.exception;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends BvsException {
    
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("RESOURCE_NOT_FOUND", 
              String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}

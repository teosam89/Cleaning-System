package com.cleaningsystem.backend.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceName;
    private final Object resourceId;
    
    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = "Resource";
        this.resourceId = "unknown";
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public Object getResourceId() {
        return resourceId;
    }
}
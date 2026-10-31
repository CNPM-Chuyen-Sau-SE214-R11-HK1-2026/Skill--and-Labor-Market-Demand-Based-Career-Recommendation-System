package com.cnpm.careerrecommendation.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested domain entity or resource is not found (HTTP 404).
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                String.format("%s không tìm thấy với %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

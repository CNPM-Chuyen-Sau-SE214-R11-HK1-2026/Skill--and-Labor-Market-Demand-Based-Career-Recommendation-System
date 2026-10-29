package com.cnpm.careerrecommendation.exception;

import org.springframework.http.HttpStatus;

/**
 * Base custom business exception conforming to coding conventions section 2.5.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ApiException(HttpStatus status, String message) {
        this(status, status.getReasonPhrase(), message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

package com.cnpm.careerrecommendation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard centralized error response structure conforming to API Contract section 1.2.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ValidationErrorDetail> errors
) {
    public ErrorResponse(int status, String error, String message, String path, List<ValidationErrorDetail> errors) {
        this(Instant.now(), status, error, message, path, errors);
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null);
    }
}

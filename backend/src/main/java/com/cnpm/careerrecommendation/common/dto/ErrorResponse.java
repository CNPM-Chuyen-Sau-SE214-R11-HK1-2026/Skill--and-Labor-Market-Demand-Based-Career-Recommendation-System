package com.cnpm.careerrecommendation.common.dto;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ErrorResponse(Instant timestamp, int status, String error, String message,
                            String path, List<FieldError> errors) {
    public record FieldError(String field, Object rejectedValue, String reason) { }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                message, path, List.of());
    }
}

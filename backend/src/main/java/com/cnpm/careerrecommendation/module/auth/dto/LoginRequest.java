package com.cnpm.careerrecommendation.module.auth.dto;

import com.cnpm.careerrecommendation.common.validation.Utf8Password;
import jakarta.validation.constraints.*;
import java.util.Locale;

public record LoginRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) @Utf8Password String password) {
    public LoginRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() { return "LoginRequest[REDACTED]"; }
}

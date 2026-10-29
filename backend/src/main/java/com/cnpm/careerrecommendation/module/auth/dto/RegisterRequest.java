package com.cnpm.careerrecommendation.module.auth.dto;

import com.cnpm.careerrecommendation.common.validation.Utf8Password;
import jakarta.validation.constraints.*;
import java.util.Locale;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 64) @Utf8Password String password,
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String phone) {
    public RegisterRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        fullName = fullName == null ? null : fullName.strip();
        phone = phone == null || phone.isBlank() ? null : phone.strip();
    }

    @Override
    public String toString() { return "RegisterRequest[REDACTED]"; }
}

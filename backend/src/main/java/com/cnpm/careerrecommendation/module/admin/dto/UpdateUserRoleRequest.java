package com.cnpm.careerrecommendation.module.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRoleRequest(
        @NotBlank(message = "role is required") String role
) {
}

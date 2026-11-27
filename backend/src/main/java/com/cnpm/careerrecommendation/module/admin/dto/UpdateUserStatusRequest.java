package com.cnpm.careerrecommendation.module.admin.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "active is required") Boolean active
) {
}

package com.cnpm.careerrecommendation.module.admin.dto;

import com.cnpm.careerrecommendation.common.entity.Role;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}

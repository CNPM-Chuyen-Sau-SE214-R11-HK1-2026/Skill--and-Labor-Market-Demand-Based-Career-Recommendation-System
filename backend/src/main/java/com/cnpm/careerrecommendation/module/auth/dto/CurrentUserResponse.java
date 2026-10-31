package com.cnpm.careerrecommendation.module.auth.dto;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import com.cnpm.careerrecommendation.module.auth.entity.User;
import java.time.Instant;

public record CurrentUserResponse(Long id, String email, String fullName, String phone,
                                  Role role, String status, Instant createdAt) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                user.getRole(), user.isActive() ? "ACTIVE" : "INACTIVE", user.getCreatedAt());
    }
}

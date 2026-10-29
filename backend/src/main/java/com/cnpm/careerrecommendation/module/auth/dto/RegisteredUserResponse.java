package com.cnpm.careerrecommendation.module.auth.dto;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import com.cnpm.careerrecommendation.module.auth.entity.User;
import java.time.Instant;

public record RegisteredUserResponse(Long id, String email, String fullName,
                                     Role role, String status, Instant createdAt) {
    public static RegisteredUserResponse from(User user) {
        return new RegisteredUserResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.isActive() ? "ACTIVE" : "INACTIVE", user.getCreatedAt());
    }
}

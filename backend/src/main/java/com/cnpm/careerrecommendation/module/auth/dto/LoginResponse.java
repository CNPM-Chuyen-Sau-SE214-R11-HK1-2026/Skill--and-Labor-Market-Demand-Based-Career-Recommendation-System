package com.cnpm.careerrecommendation.module.auth.dto;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import com.cnpm.careerrecommendation.module.auth.entity.User;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, LoginUser user) {
    public record LoginUser(Long id, String email, String fullName, Role role) {
        public static LoginUser from(User user) {
            return new LoginUser(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
        }
    }

    @Override
    public String toString() { return "LoginResponse[REDACTED]"; }
}

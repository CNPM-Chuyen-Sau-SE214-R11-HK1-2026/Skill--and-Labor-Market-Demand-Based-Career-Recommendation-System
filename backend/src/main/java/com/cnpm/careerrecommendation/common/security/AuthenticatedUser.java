package com.cnpm.careerrecommendation.common.security;

import com.cnpm.careerrecommendation.module.auth.entity.Role;
import java.security.Principal;

/** Identity for downstream modules; userId must come from this authenticated principal. */
public record AuthenticatedUser(Long userId, String email, Role role) implements Principal {
    @Override
    public String getName() { return email; }
}

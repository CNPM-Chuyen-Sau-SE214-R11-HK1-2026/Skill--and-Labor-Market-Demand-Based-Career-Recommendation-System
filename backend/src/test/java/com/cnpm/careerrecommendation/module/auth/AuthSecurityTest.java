package com.cnpm.careerrecommendation.module.auth;

import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.auth.controller.AuthController;
import com.cnpm.careerrecommendation.module.auth.dto.CurrentUserResponse;
import com.cnpm.careerrecommendation.module.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, AdminSmokeTestController.class})
@Import({SecurityConfig.class, CorsProperties.class, JwtTokenProvider.class, SecurityErrorHandler.class})
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret-key-that-is-at-least-32-bytes-long-for-hmac",
        "security.jwt.expiration-ms=86400000"
})
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/v1/auth/me without token returns 401 with standard ErrorResponse JSON")
    void meWithoutToken_Returns401Json() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with invalid token returns 401 with standard ErrorResponse JSON")
    void meWithInvalidToken_Returns401Json() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/me"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me with valid token returns 200 and no password hash")
    void meWithValidToken_Returns200WithoutPasswordHash() throws Exception {
        User user = User.candidate("candidate@example.com", "HashedPassword123", "Nguyễn Văn A", "0901234567");
        ReflectionTestUtils.setField(user, "id", 10L);

        when(userRepository.findById(eq(10L))).thenReturn(Optional.of(user));
        when(authService.currentUser(any())).thenReturn(
                new CurrentUserResponse(10L, "candidate@example.com", "Nguyễn Văn A", "0901234567",
                        Role.ROLE_CANDIDATE, "ACTIVE", Instant.now())
        );

        String token = jwtTokenProvider.generateToken(user);

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("candidate@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.role").value("ROLE_CANDIDATE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("Method security smoke test: Candidate accessing ADMIN-only endpoint returns 403 Forbidden JSON")
    @WithMockUser(roles = "CANDIDATE")
    void candidateHitsAdminOnlyEndpoint_Returns403Json() throws Exception {
        mockMvc.perform(get("/api/test/admin-only")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/api/test/admin-only"));
    }
}

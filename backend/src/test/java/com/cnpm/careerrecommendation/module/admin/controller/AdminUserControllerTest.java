package com.cnpm.careerrecommendation.module.admin.controller;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.admin.dto.AdminStatsResponse;
import com.cnpm.careerrecommendation.module.admin.dto.AdminUserResponse;
import com.cnpm.careerrecommendation.module.admin.service.AdminService;
import java.time.Instant;
import java.util.List;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, CorsProperties.class, SecurityErrorHandler.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AdminService adminService;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean UserRepository userRepository;

    @Test
    void anonymousCannotReadUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(adminService, never()).listUsers(any(), any(), any(), any());
    }

    @Test
    void invalidTokenCannotReadUsers() throws Exception {
        when(jwtTokenProvider.parseToken("invalid-token"))
                .thenThrow(new MalformedJwtException("invalid token"));
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(adminService, never()).listUsers(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void candidateCannotAccessAnyAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/stats")).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/admin/users/2/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsersReturnsPageWithoutPasswordHash() throws Exception {
        AdminUserResponse user = response(2L, Role.ROLE_CANDIDATE, true);
        when(adminService.listUsers(eq("candidate"), eq("ROLE_CANDIDATE"), eq(true), any()))
                .thenReturn(PageResponse.of(List.of(user), 0, 10, 1, 1));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("email", "candidate")
                        .param("role", "ROLE_CANDIDATE")
                        .param("status", "ACTIVE")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listUsersRejectsInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PAGINATION"));
        verify(adminService, never()).listUsers(any(), any(), any(), any());
    }

    @Test
    void adminCanUpdateOtherUserStatus() throws Exception {
        when(adminService.updateUserStatus(eq(2L), any(), eq(1L)))
                .thenReturn(response(2L, Role.ROLE_CANDIDATE, false));

        mockMvc.perform(patch("/api/v1/admin/users/2/status")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void selfDeactivationReturns400() throws Exception {
        when(adminService.updateUserStatus(eq(1L), any(), eq(1L)))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST,
                        "SELF_DEACTIVATION_NOT_ALLOWED", "Admin cannot deactivate their own account"));

        mockMvc.perform(patch("/api/v1/admin/users/1/status")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SELF_DEACTIVATION_NOT_ALLOWED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateRole() throws Exception {
        when(adminService.updateUserRole(eq(2L), any())).thenReturn(response(2L, Role.ROLE_ADMIN, true));

        mockMvc.perform(patch("/api/v1/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidRoleBodyReturns400WithoutCallingService() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("role"));
        verify(adminService, never()).updateUserRole(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadStats() throws Exception {
        when(adminService.getStats()).thenReturn(new AdminStatsResponse(10, 8, 7, 6, 5, 4));

        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalProfiles").value(8))
                .andExpect(jsonPath("$.totalRecommendationLogs").value(4));
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AuthenticatedUser principal = new AuthenticatedUser(1L, "admin@example.com", Role.ROLE_ADMIN);
        return new UsernamePasswordAuthenticationToken(principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private AdminUserResponse response(Long id, Role role, boolean active) {
        return new AdminUserResponse(id, "candidate@example.com", "Candidate", role, active,
                Instant.parse("2026-11-01T00:00:00Z"), Instant.parse("2026-11-02T00:00:00Z"));
    }
}

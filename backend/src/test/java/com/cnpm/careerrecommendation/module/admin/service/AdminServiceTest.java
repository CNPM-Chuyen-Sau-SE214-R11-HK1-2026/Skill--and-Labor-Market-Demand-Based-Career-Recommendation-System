package com.cnpm.careerrecommendation.module.admin.service;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.entity.User;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.repository.SkillRepository;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.module.admin.dto.AdminStatsResponse;
import com.cnpm.careerrecommendation.module.admin.dto.AdminUserResponse;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserRoleRequest;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserStatusRequest;
import com.cnpm.careerrecommendation.repository.CandidateProfileRepository;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import com.cnpm.careerrecommendation.repository.RecommendationLogRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock CandidateProfileRepository candidateProfileRepository;
    @Mock OccupationRepository occupationRepository;
    @Mock SkillRepository skillRepository;
    @Mock MarketDemandIndicatorRepository marketDemandIndicatorRepository;
    @Mock RecommendationLogRepository recommendationLogRepository;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(userRepository, candidateProfileRepository, occupationRepository,
                skillRepository, marketDemandIndicatorRepository, recommendationLogRepository);
    }

    @Test
    void listUsersAppliesCombinedFiltersAndMapsPage() {
        PageRequest pageable = PageRequest.of(1, 2);
        User user = user(7L, "Admin@Example.com", Role.ROLE_ADMIN, true);
        when(userRepository.searchAdminUsers("Admin", Role.ROLE_ADMIN, true, pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 5));

        PageResponse<AdminUserResponse> result = service.listUsers(" Admin ", "ROLE_ADMIN", true, pageable);

        assertEquals(1, result.content().size());
        assertEquals(7L, result.content().get(0).id());
        assertEquals(5, result.totalElements());
        assertFalse(result.last());
    }

    @Test
    void listUsersWithoutFiltersReturnsEmptyPage() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRepository.searchAdminUsers(null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<AdminUserResponse> result = service.listUsers(null, null, null, pageable);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void listUsersRejectsInvalidRoleBeforeQuery() {
        ApiException error = assertThrows(ApiException.class,
                () -> service.listUsers(null, "SUPER_ADMIN", null, PageRequest.of(0, 20)));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("INVALID_ROLE", error.getErrorCode());
        verify(userRepository, never()).searchAdminUsers(any(), any(), any(), any());
    }

    @Test
    void listUsersPassesCandidateAndInactiveFilters() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.searchAdminUsers(null, Role.ROLE_CANDIDATE, false, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.listUsers(" ", "ROLE_CANDIDATE", false, pageable);

        verify(userRepository).searchAdminUsers(null, Role.ROLE_CANDIDATE, false, pageable);
    }

    @Test
    void updateStatusDeactivatesOtherUser() {
        User target = user(2L, "candidate@example.com", Role.ROLE_CANDIDATE, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        AdminUserResponse result = service.updateUserStatus(2L, new UpdateUserStatusRequest(false), 1L);

        assertFalse(result.active());
        verify(userRepository).save(target);
    }

    @Test
    void updateStatusActivatesOtherUser() {
        User target = user(2L, "candidate@example.com", Role.ROLE_CANDIDATE, false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        AdminUserResponse result = service.updateUserStatus(2L, new UpdateUserStatusRequest(true), 1L);

        assertTrue(result.active());
        verify(userRepository).save(target);
    }

    @Test
    void updateStatusIsIdempotent() {
        User target = user(2L, "candidate@example.com", Role.ROLE_CANDIDATE, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        AdminUserResponse result = service.updateUserStatus(2L, new UpdateUserStatusRequest(true), 1L);

        assertTrue(result.active());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateStatusRejectsSelfDeactivationWithoutSave() {
        User admin = user(1L, "admin@example.com", Role.ROLE_ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        ApiException error = assertThrows(ApiException.class,
                () -> service.updateUserStatus(1L, new UpdateUserStatusRequest(false), 1L));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("SELF_DEACTIVATION_NOT_ALLOWED", error.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateStatusReturnsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ApiException error = assertThrows(ApiException.class,
                () -> service.updateUserStatus(99L, new UpdateUserStatusRequest(false), 1L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("USER_NOT_FOUND", error.getErrorCode());
    }

    @Test
    void updateRoleChangesValidRole() {
        User target = user(2L, "candidate@example.com", Role.ROLE_CANDIDATE, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        AdminUserResponse result = service.updateUserRole(2L, new UpdateUserRoleRequest("ROLE_ADMIN"));

        assertEquals(Role.ROLE_ADMIN, result.role());
        verify(userRepository).save(target);
    }

    @Test
    void updateRoleReturnsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        ApiException error = assertThrows(ApiException.class,
                () -> service.updateUserRole(99L, new UpdateUserRoleRequest("ROLE_ADMIN")));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("USER_NOT_FOUND", error.getErrorCode());
    }

    @Test
    void updateRoleRejectsBlankValueBeforeDatabaseAccess() {
        ApiException error = assertThrows(ApiException.class,
                () -> service.updateUserRole(2L, new UpdateUserRoleRequest(" ")));
        assertEquals("INVALID_ROLE", error.getErrorCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateRoleRejectsInvalidValueBeforeDatabaseAccess() {
        ApiException error = assertThrows(ApiException.class,
                () -> service.updateUserRole(2L, new UpdateUserRoleRequest("MASTER")));
        assertEquals("INVALID_ROLE", error.getErrorCode());
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRoleIsIdempotent() {
        User target = user(2L, "admin@example.com", Role.ROLE_ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        AdminUserResponse result = service.updateUserRole(2L, new UpdateUserRoleRequest("role_admin"));

        assertEquals(Role.ROLE_ADMIN, result.role());
        verify(userRepository, never()).save(any());
    }

    @Test
    void statsMapsEveryRepositoryCount() {
        when(userRepository.count()).thenReturn(10L);
        when(candidateProfileRepository.count()).thenReturn(8L);
        when(occupationRepository.count()).thenReturn(7L);
        when(skillRepository.count()).thenReturn(6L);
        when(marketDemandIndicatorRepository.count()).thenReturn(5L);
        when(recommendationLogRepository.count()).thenReturn(4L);

        AdminStatsResponse stats = service.getStats();

        assertEquals(new AdminStatsResponse(10, 8, 7, 6, 5, 4), stats);
    }

    @Test
    void statsReturnsZerosForEmptyDatabase() {
        AdminStatsResponse stats = service.getStats();
        assertEquals(new AdminStatsResponse(0, 0, 0, 0, 0, 0), stats);
    }

    private User user(Long id, String email, Role role, boolean active) {
        User user = new User(email, "secret-hash", "Test User", role);
        user.setId(id);
        user.setActive(active);
        user.setCreatedAt(Instant.parse("2026-11-01T00:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-11-02T00:00:00Z"));
        return user;
    }
}

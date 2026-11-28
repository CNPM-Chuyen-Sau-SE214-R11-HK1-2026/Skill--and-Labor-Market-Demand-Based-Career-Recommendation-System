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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final OccupationRepository occupationRepository;
    private final SkillRepository skillRepository;
    private final MarketDemandIndicatorRepository marketDemandIndicatorRepository;
    private final RecommendationLogRepository recommendationLogRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            CandidateProfileRepository candidateProfileRepository,
                            OccupationRepository occupationRepository,
                            SkillRepository skillRepository,
                            MarketDemandIndicatorRepository marketDemandIndicatorRepository,
                            RecommendationLogRepository recommendationLogRepository) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.occupationRepository = occupationRepository;
        this.skillRepository = skillRepository;
        this.marketDemandIndicatorRepository = marketDemandIndicatorRepository;
        this.recommendationLogRepository = recommendationLogRepository;
    }

    @Override
    public PageResponse<AdminUserResponse> listUsers(String email, String role, Boolean active, Pageable pageable) {
        validatePageable(pageable);
        String normalizedEmail = normalize(email);
        Role normalizedRole = parseRole(role, true);
        Page<User> users = userRepository.searchAdminUsers(normalizedEmail, normalizedRole, active, pageable);
        return PageResponse.from(users.map(this::toResponse));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, Long currentAdminId) {
        if (request == null || request.active() == null) {
            throw badRequest("INVALID_USER_STATUS", "active is required");
        }
        User user = findUser(userId);
        if (userId.equals(currentAdminId) && !request.active()) {
            throw badRequest("SELF_DEACTIVATION_NOT_ALLOWED", "Admin cannot deactivate their own account");
        }
        if (user.isActive() != request.active()) {
            user.setActive(request.active());
            user = userRepository.save(user);
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, Long currentAdminId) {
        Role role = parseRole(request == null ? null : request.role(), false);
        User user = findUser(userId);
        if (userId.equals(currentAdminId) && role == Role.ROLE_CANDIDATE) {
            throw badRequest("SELF_ROLE_DOWNGRADE_NOT_ALLOWED",
                    "Admin cannot downgrade their own role");
        }
        if (user.getRole() != role) {
            user.setRole(role);
            user = userRepository.save(user);
        }
        return toResponse(user);
    }

    @Override
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                candidateProfileRepository.count(),
                occupationRepository.count(),
                skillRepository.count(),
                marketDemandIndicatorRepository.count(),
                recommendationLogRepository.count()
        );
    }

    private User findUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }

    private Role parseRole(String value, boolean nullable) {
        String normalized = normalize(value);
        if (normalized == null && nullable) {
            return null;
        }
        if (normalized == null) {
            throw badRequest("INVALID_ROLE", "role is required");
        }
        try {
            return Role.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw badRequest("INVALID_ROLE", "Unsupported role");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null || pageable.getPageNumber() < 0
                || pageable.getPageSize() < 1 || pageable.getPageSize() > 100
                || pageable.getOffset() > Integer.MAX_VALUE) {
            throw badRequest("INVALID_PAGINATION", "page must be non-negative and size must be between 1 and 100");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ApiException badRequest(String code, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, message);
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive(),
                user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}

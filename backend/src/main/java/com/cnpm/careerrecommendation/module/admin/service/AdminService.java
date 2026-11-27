package com.cnpm.careerrecommendation.module.admin.service;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.module.admin.dto.AdminStatsResponse;
import com.cnpm.careerrecommendation.module.admin.dto.AdminUserResponse;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserRoleRequest;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserStatusRequest;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    PageResponse<AdminUserResponse> listUsers(String email, String role, Boolean active, Pageable pageable);

    AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, Long currentAdminId);

    AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, Long currentAdminId);

    AdminStatsResponse getStats();
}

package com.cnpm.careerrecommendation.module.admin.controller;

import com.cnpm.careerrecommendation.common.dto.ErrorResponse;
import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.module.admin.dto.AdminStatsResponse;
import com.cnpm.careerrecommendation.module.admin.dto.AdminUserResponse;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserRoleRequest;
import com.cnpm.careerrecommendation.module.admin.dto.UpdateUserStatusRequest;
import com.cnpm.careerrecommendation.module.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final int MAX_PAGE_SIZE = 100;
    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserResponse>> listUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(
                email, role, resolveActive(active, status), pageRequest(page, size)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentAdmin) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, request, currentAdminId(currentAdmin)));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentAdmin) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request, currentAdminId(currentAdmin)));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE
                || (long) page * size > Integer.MAX_VALUE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGINATION",
                    "page must be non-negative and size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> invalidParameter(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(Instant.now(), 400,
                "INVALID_ADMIN_REQUEST", "Query or path parameter has an invalid type",
                request.getRequestURI(), List.of()));
    }

    private Long currentAdminId(AuthenticatedUser currentAdmin) {
        if (currentAdmin == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authenticated admin is required");
        }
        return currentAdmin.userId();
    }

    private Boolean resolveActive(Boolean active, String status) {
        if (status == null || status.isBlank()) {
            return active;
        }
        Boolean fromStatus = switch (status.trim().toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USER_STATUS",
                    "status must be ACTIVE or INACTIVE");
        };
        if (active != null && !active.equals(fromStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USER_STATUS",
                    "active and status filters conflict");
        }
        return fromStatus;
    }
}

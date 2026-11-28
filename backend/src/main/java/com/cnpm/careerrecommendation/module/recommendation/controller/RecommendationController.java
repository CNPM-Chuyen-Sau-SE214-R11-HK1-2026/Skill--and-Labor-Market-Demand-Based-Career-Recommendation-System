package com.cnpm.careerrecommendation.module.recommendation.controller;

import com.cnpm.careerrecommendation.common.dto.ErrorResponse;
import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationHistoryItemResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.SkillGapResponse;
import com.cnpm.careerrecommendation.module.recommendation.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/v1/recommendations")
@PreAuthorize("hasRole('CANDIDATE')")
public class RecommendationController {
  private final RecommendationService service;

  public RecommendationController(RecommendationService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<RecommendationResponse>> getRecommendations(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(service.getRecommendations(requireUserId(currentUser), limit));
  }

  @GetMapping("/{occupationId}/skill-gap")
  public ResponseEntity<SkillGapResponse> getSkillGap(
      @AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable Long occupationId) {
    return ResponseEntity.ok(service.getSkillGap(requireUserId(currentUser), occupationId));
  }

  @GetMapping("/history")
  public ResponseEntity<PageResponse<RecommendationHistoryItemResponse>> getHistory(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(
        service.getRecommendationHistory(requireUserId(currentUser), page, size));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return ResponseEntity.badRequest()
        .body(
            new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_PARAMETER",
                "Invalid value for parameter '" + ex.getName() + "'.",
                request.getRequestURI(),
                List.of()));
  }

  private Long requireUserId(AuthenticatedUser currentUser) {
    if (currentUser == null || currentUser.userId() == null) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authenticated candidate is required.");
    }
    return currentUser.userId();
  }
}

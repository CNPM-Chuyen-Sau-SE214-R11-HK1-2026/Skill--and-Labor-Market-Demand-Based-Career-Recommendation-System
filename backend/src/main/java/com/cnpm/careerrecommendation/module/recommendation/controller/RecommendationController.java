package com.cnpm.careerrecommendation.module.recommendation.controller;

import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.SkillGapResponse;
import com.cnpm.careerrecommendation.module.recommendation.service.RecommendationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    return ResponseEntity.ok(service.getRecommendations(currentUser.userId(), limit));
  }

  @GetMapping("/{occupationId}/skill-gap")
  public ResponseEntity<SkillGapResponse> getSkillGap(
      @AuthenticationPrincipal AuthenticatedUser currentUser, @PathVariable Long occupationId) {
    return ResponseEntity.ok(service.getSkillGap(currentUser.userId(), occupationId));
  }
}

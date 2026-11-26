package com.cnpm.careerrecommendation.module.recommendation.service;

import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.SkillGapResponse;
import java.util.List;

public interface RecommendationService {
  List<RecommendationResponse> getRecommendations(Long userId, int limit);

  SkillGapResponse getSkillGap(Long userId, Long occupationId);
}

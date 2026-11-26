package com.cnpm.careerrecommendation.module.recommendation.service;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationHistoryItemResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationResponse;
import com.cnpm.careerrecommendation.module.recommendation.dto.SkillGapResponse;
import java.util.List;

public interface RecommendationService {
  List<RecommendationResponse> getRecommendations(Long userId, int limit);

  SkillGapResponse getSkillGap(Long userId, Long occupationId);

  PageResponse<RecommendationHistoryItemResponse> getRecommendationHistory(
      Long userId, int page, int size);
}

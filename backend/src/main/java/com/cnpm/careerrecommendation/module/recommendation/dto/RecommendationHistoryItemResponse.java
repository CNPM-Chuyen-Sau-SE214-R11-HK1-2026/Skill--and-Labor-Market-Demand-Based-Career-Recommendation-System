package com.cnpm.careerrecommendation.module.recommendation.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;

public record RecommendationHistoryItemResponse(
    Long id,
    Long occupationId,
    String occupationCode,
    String occupationTitle,
    String groupName,
    BigDecimal matchScore,
    int skillGapCount,
    Instant recommendationDate,
    Instant createdAt,
    JsonNode inputSnapshot) {}

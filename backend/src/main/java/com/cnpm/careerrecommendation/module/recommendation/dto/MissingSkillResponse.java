package com.cnpm.careerrecommendation.module.recommendation.dto;

import java.math.BigDecimal;

public record MissingSkillResponse(
    Long skillId,
    String name,
    String requiredImportance,
    BigDecimal importanceWeight,
    String priority,
    String recommendationNote) {}

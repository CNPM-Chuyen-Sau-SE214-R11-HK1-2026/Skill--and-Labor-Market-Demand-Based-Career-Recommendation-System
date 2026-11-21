package com.cnpm.careerrecommendation.module.recommendation.dto;

import java.math.BigDecimal;

public record MatchedSkillResponse(
    Long skillId,
    String name,
    String userProficiency,
    String requiredImportance,
    BigDecimal importanceWeight) {}

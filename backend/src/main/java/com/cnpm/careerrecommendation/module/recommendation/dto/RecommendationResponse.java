package com.cnpm.careerrecommendation.module.recommendation.dto;

public record RecommendationResponse(
    int rank,
    Long occupationId,
    String occupationCode,
    String occupationTitle,
    String groupName,
    double finalScore,
    ScoreBreakdownResponse scoreBreakdown,
    String marketGrowth,
    int matchedSkillsCount,
    int totalRequiredSkillsCount,
    String explanation) {}

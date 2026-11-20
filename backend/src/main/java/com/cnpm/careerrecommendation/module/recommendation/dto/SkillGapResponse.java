package com.cnpm.careerrecommendation.module.recommendation.dto;

import java.util.List;

public record SkillGapResponse(
    Long occupationId,
    String occupationTitle,
    double matchPercentage,
    List<MatchedSkillResponse> matchedSkills,
    List<MissingSkillResponse> missingEssentialSkills,
    List<MissingSkillResponse> missingOptionalSkills) {}

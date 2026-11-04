package com.cnpm.careerrecommendation.module.profile.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfileResponse(
        Long id, Long userId, String headline, BigDecimal yearsOfExperience,
        BigDecimal expectedSalaryMin, BigDecimal expectedSalaryMax,
        String preferredLocation, String workMode, String targetIndustry,
        long skillsCount, Instant updatedAt) {
}

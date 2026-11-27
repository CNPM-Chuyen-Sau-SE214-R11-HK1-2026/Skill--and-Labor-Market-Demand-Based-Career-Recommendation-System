package com.cnpm.careerrecommendation.module.taxonomy.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO returned after administrative occupation creation or update.
 */
public record AdminOccupationResponse(
        Long id,
        String code,
        String title,
        Long groupId,
        String groupName,
        String description,
        String industry,
        BigDecimal entrySalaryAvg,
        BigDecimal midSalaryAvg,
        Instant createdAt,
        Instant updatedAt
) {
}

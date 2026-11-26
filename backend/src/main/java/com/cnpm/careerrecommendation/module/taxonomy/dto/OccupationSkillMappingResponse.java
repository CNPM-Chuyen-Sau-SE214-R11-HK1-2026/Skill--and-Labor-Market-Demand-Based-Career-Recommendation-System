package com.cnpm.careerrecommendation.module.taxonomy.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO representing an occupation-to-skill mapping entry.
 */
public record OccupationSkillMappingResponse(
        Long id,
        Long occupationId,
        String occupationCode,
        String occupationTitle,
        Long skillId,
        String skillCode,
        String skillName,
        BigDecimal importanceWeight,
        Boolean isRequired,
        Instant createdAt,
        Instant updatedAt
) {
}

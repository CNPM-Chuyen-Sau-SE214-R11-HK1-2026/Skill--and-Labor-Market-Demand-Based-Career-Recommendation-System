package com.cnpm.careerrecommendation.module.taxonomy.dto;

import java.time.Instant;

/**
 * Response DTO returned after administrative skill creation or update.
 */
public record AdminSkillResponse(
        Long id,
        String code,
        String name,
        Long categoryId,
        String categoryName,
        String skillType,
        String description,
        String standardSource,
        Instant createdAt,
        Instant updatedAt
) {
}

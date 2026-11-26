package com.cnpm.careerrecommendation.module.taxonomy.dto;

/**
 * DTO representing a skill category for administrative forms and filters.
 */
public record SkillCategoryResponse(
        Long id,
        String code,
        String name,
        String description
) {
}

package com.cnpm.careerrecommendation.module.taxonomy.dto;

/**
 * Standardized skill dictionary entry DTO.
 * Matches docs/api/taxonomy.md contract.
 */
public record SkillDictionaryResponse(
    Long id,
    String code,
    String name,
    String categoryName,
    String standardSource
) {}

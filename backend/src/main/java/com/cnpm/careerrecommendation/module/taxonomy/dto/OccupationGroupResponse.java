package com.cnpm.careerrecommendation.module.taxonomy.dto;

/**
 * DTO representing an occupation group for administrative forms and filters.
 */
public record OccupationGroupResponse(
        Long id,
        String code,
        String name,
        String description
) {
}

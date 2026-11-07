package com.cnpm.careerrecommendation.module.taxonomy.dto;

import java.math.BigDecimal;

/**
 * Summary DTO for occupations returned in paginated search results.
 * Matches docs/api/taxonomy.md contract.
 */
public record OccupationSummaryResponse(
    Long id,
    String code,
    String title,
    String groupName,
    BigDecimal averageSalaryMin,
    BigDecimal averageSalaryMax,
    BigDecimal demandScore,
    BigDecimal growthRatePct
) {}

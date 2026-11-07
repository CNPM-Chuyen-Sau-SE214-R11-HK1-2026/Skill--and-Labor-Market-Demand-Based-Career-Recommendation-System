package com.cnpm.careerrecommendation.module.taxonomy.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Detailed DTO for an occupation including salary range, market indicators, and required skills.
 * Matches docs/api/taxonomy.md contract.
 */
public record OccupationDetailResponse(
    Long id,
    String code,
    String title,
    String groupName,
    String description,
    SalaryRangeDto salaryRange,
    MarketIndicatorDto marketIndicators,
    List<OccupationSkillRequirementDto> skills
) {

    public record SalaryRangeDto(
        BigDecimal min,
        BigDecimal max,
        String currency
    ) {}

    public record MarketIndicatorDto(
        BigDecimal demandScore,
        BigDecimal growthRatePct,
        String shortageLevel,
        Integer openingsCount,
        String dataPeriod,
        String sourceName
    ) {}

    public record OccupationSkillRequirementDto(
        Long skillId,
        String skillCode,
        String skillName,
        String importanceType,
        Double weight
    ) {}
}

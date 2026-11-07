package com.cnpm.careerrecommendation.module.profile.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AddSkillRequest(
        @NotNull @Positive @Digits(integer = 19, fraction = 0) @Max(Long.MAX_VALUE) BigDecimal skillId,
        @NotNull @Pattern(regexp = PROFICIENCY_LEVELS) String proficiencyLevel,
        @PositiveOrZero @Digits(integer = 3, fraction = 1) BigDecimal yearsExperience) {

    static final String PROFICIENCY_LEVELS = "BEGINNER|INTERMEDIATE|ADVANCED|EXPERT";
}

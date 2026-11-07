package com.cnpm.careerrecommendation.module.profile.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record UpdateSkillRequest(
        @Pattern(regexp = AddSkillRequest.PROFICIENCY_LEVELS) String proficiencyLevel,
        @PositiveOrZero @Digits(integer = 3, fraction = 1) BigDecimal yearsExperience) {
}

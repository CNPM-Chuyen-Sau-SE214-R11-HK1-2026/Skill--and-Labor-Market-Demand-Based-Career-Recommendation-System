package com.cnpm.careerrecommendation.module.profile.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProfileRequest(
        @Size(max = 255) String headline,
        @PositiveOrZero @Digits(integer = 3, fraction = 1) BigDecimal yearsOfExperience,
        @Digits(integer = 10, fraction = 2) BigDecimal expectedSalaryMin,
        @Digits(integer = 10, fraction = 2) BigDecimal expectedSalaryMax,
        @Size(max = 100) String preferredLocation,
        @Pattern(regexp = "REMOTE|HYBRID|ONSITE") String workMode,
        @Size(max = 100) String targetIndustry) {
}

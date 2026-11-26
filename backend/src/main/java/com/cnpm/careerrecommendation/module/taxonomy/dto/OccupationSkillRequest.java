package com.cnpm.careerrecommendation.module.taxonomy.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request DTO for assigning a skill (with importanceWeight and isRequired) to an occupation.
 */
public record OccupationSkillRequest(
        @NotNull(message = "skillId không được để trống")
        @Positive(message = "skillId phải là số nguyên dương")
        @Digits(integer = 19, fraction = 0, message = "skillId phải là số nguyên dương")
        @Max(value = Long.MAX_VALUE, message = "skillId vượt quá giới hạn cho phép")
        BigDecimal skillId,

        @DecimalMin(value = "0.00", message = "importanceWeight phải nằm trong khoảng 0.00 đến 1.00")
        @DecimalMax(value = "1.00", message = "importanceWeight phải nằm trong khoảng 0.00 đến 1.00")
        @Digits(integer = 1, fraction = 2, message = "importanceWeight tối đa 1 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal importanceWeight,

        Boolean isRequired
) {
}

package com.cnpm.careerrecommendation.module.taxonomy.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a standardized skill by an administrator.
 */
public record AdminSkillRequest(
        @NotBlank(message = "code không được để trống")
        @Size(max = 50, message = "code tối đa 50 ký tự")
        String code,

        @NotBlank(message = "name không được để trống")
        @Size(max = 150, message = "name tối đa 150 ký tự")
        String name,

        @Positive(message = "categoryId phải là số nguyên dương")
        @Digits(integer = 19, fraction = 0, message = "categoryId phải là số nguyên dương")
        @Max(value = Long.MAX_VALUE, message = "categoryId vượt quá giới hạn cho phép")
        BigDecimal categoryId,

        @Size(max = 30, message = "skillType tối đa 30 ký tự")
        String skillType,

        String description
) {
}

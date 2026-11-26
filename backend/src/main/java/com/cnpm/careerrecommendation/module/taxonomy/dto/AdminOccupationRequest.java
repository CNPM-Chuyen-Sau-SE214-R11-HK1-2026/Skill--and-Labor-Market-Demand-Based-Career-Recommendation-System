package com.cnpm.careerrecommendation.module.taxonomy.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating an occupation by an administrator.
 */
public record AdminOccupationRequest(
        @NotBlank(message = "code không được để trống")
        @Size(max = 50, message = "code tối đa 50 ký tự")
        String code,

        @NotBlank(message = "title không được để trống")
        @Size(max = 200, message = "title tối đa 200 ký tự")
        String title,

        @Positive(message = "groupId phải là số nguyên dương")
        @Digits(integer = 19, fraction = 0, message = "groupId phải là số nguyên dương")
        @Max(value = Long.MAX_VALUE, message = "groupId vượt quá giới hạn cho phép")
        BigDecimal groupId,

        String description,

        @Size(max = 100, message = "industry tối đa 100 ký tự")
        String industry,

        @DecimalMin(value = "0.00", message = "entrySalaryAvg không được âm")
        @DecimalMax(value = "9999999999.99", message = "entrySalaryAvg vượt quá giới hạn cho phép")
        @Digits(integer = 10, fraction = 2, message = "entrySalaryAvg tối đa 10 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal entrySalaryAvg,

        @DecimalMin(value = "0.00", message = "midSalaryAvg không được âm")
        @DecimalMax(value = "9999999999.99", message = "midSalaryAvg vượt quá giới hạn cho phép")
        @Digits(integer = 10, fraction = 2, message = "midSalaryAvg tối đa 10 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal midSalaryAvg
) {
}

package com.cnpm.careerrecommendation.module.taxonomy.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;

/**
 * Request DTO for updating weight and required status of an existing occupation-skill mapping.
 */
public record UpdateOccupationSkillRequest(
        @DecimalMin(value = "0.00", message = "importanceWeight phải nằm trong khoảng 0.00 đến 1.00")
        @DecimalMax(value = "1.00", message = "importanceWeight phải nằm trong khoảng 0.00 đến 1.00")
        @Digits(integer = 1, fraction = 2, message = "importanceWeight tối đa 1 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal importanceWeight,

        Boolean isRequired
) {
}

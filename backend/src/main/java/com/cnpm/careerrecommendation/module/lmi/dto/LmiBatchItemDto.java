package com.cnpm.careerrecommendation.module.lmi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Single labor market indicator item within a batch import request.
 * At least one of {@code occupationId} or {@code occupationCode} must be supplied.
 * Integer fields use {@link BigDecimal} with {@code fraction = 0} so Jackson rejects fractional JSON numbers.
 */
public record LmiBatchItemDto(
        @Positive(message = "occupationId phải là số nguyên dương")
        @Digits(integer = 19, fraction = 0, message = "occupationId phải là số nguyên dương")
        @Max(value = Long.MAX_VALUE, message = "occupationId vượt quá giới hạn cho phép")
        BigDecimal occupationId,

        @Size(max = 50, message = "occupationCode tối đa 50 ký tự")
        String occupationCode,

        @Size(max = 50, message = "region tối đa 50 ký tự")
        String region,

        @NotNull(message = "demandScore không được để trống")
        @DecimalMin(value = "0.00", message = "demandScore phải nằm trong khoảng 0 đến 100")
        @DecimalMax(value = "100.00", message = "demandScore phải nằm trong khoảng 0 đến 100")
        @Digits(integer = 3, fraction = 2, message = "demandScore tối đa 3 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal demandScore,

        @DecimalMin(value = "-999.99", message = "growthRatePct không được nhỏ hơn -999.99")
        @DecimalMax(value = "999.99", message = "growthRatePct không được lớn hơn 999.99")
        @Digits(integer = 3, fraction = 2, message = "growthRatePct tối đa 3 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal growthRatePct,

        @PositiveOrZero(message = "openJobPostingsCount không được âm")
        @Digits(integer = 10, fraction = 0, message = "openJobPostingsCount phải là số nguyên không âm")
        @Max(value = Integer.MAX_VALUE, message = "openJobPostingsCount vượt quá giới hạn cho phép")
        BigDecimal openJobPostingsCount,

        @DecimalMin(value = "0.00", message = "competitiveIndex không được âm")
        @DecimalMax(value = "9.99", message = "competitiveIndex tối đa là 9.99")
        @Digits(integer = 1, fraction = 2, message = "competitiveIndex tối đa 1 chữ số nguyên và 2 chữ số thập phân")
        BigDecimal competitiveIndex,

        @NotNull(message = "snapshotDate không được để trống")
        LocalDate snapshotDate
) {
}

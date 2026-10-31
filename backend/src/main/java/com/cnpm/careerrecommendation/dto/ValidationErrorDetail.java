package com.cnpm.careerrecommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Details of an individual field validation error matching API Contract 1.2.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationErrorDetail(
    String field,
    Object rejectedValue,
    String reason
) {}

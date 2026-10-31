package com.cnpm.careerrecommendation.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class Utf8PasswordValidator implements ConstraintValidator<Utf8Password, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || (value.indexOf('\0') < 0
                && value.getBytes(StandardCharsets.UTF_8).length <= 72);
    }
}

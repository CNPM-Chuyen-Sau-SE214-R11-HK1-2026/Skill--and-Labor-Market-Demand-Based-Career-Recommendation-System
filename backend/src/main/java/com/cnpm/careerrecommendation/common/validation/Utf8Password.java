package com.cnpm.careerrecommendation.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Utf8PasswordValidator.class)
public @interface Utf8Password {
    String message() default "Mật khẩu không được chứa ký tự NUL hoặc vượt 72 byte UTF-8";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package com.cnpm.careerrecommendation.module.auth;

import com.cnpm.careerrecommendation.module.auth.dto.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class AuthRequestValidationTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private final Validator validator = FACTORY.getValidator();

    @AfterAll
    static void closeFactory() { FACTORY.close(); }

    @ParameterizedTest
    @ValueSource(strings = {"", "short", "a\u0000bcdefgh", "ậậậậậậậậậậậậậậậậậậậậậậậậậ", "                                                                "})
    void rejectsUnsafePasswords(String password) {
        assertThat(validator.validate(new RegisterRequest("khang@example.com", password, "Việt Khang", null)))
                .anyMatch(error -> error.getPropertyPath().toString().equals("password"));
    }

    @Test
    void acceptsUnicodePasswordWithinByteLimitWithoutTrimmingIt() {
        RegisterRequest request = new RegisterRequest("  KHANG@Example.Com ", " MậtKhẩu123! ", " Việt Khang ", " 0901234567 ");
        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.email()).isEqualTo("khang@example.com");
        assertThat(request.fullName()).isEqualTo("Việt Khang");
        assertThat(request.phone()).isEqualTo("0901234567");
        assertThat(request.password()).isEqualTo(" MậtKhẩu123! ");
        assertThat(request.toString()).doesNotContain(request.password());
    }

    @Test
    void rejectsInvalidEmailAndBlankName() {
        var fields = validator.validate(new RegisterRequest("invalid", "Password123!", " ", null))
                .stream().map(error -> error.getPropertyPath().toString()).toList();
        assertThat(fields).contains("email", "fullName");
    }

    @Test
    void rejectsOverlongFields() {
        var request = new RegisterRequest("a".repeat(95) + "@x.com", "x".repeat(65), "x".repeat(101), "1".repeat(21));
        assertThat(validator.validate(request).stream().map(error -> error.getPropertyPath().toString()))
                .contains("email", "password", "fullName", "phone");
    }

    @Test
    void normalizesLoginAndRedactsItsStringRepresentation() {
        var request = new LoginRequest(" KHANG@Example.com ", "Password123!");
        assertThat(request.email()).isEqualTo("khang@example.com");
        assertThat(request.toString()).doesNotContain(request.password());
        assertThat(validator.validate(request)).isEmpty();
    }
}

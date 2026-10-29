package com.cnpm.careerrecommendation.exception;

import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GlobalExceptionHandler and Bean Validation integration.
 * Uses a dedicated test-only controller to exercise validation and exception scenarios
 * without interfering with real business controllers.
 */
@WebMvcTest(GlobalExceptionHandlerTest.TestValidationController.class)
@Import({SecurityConfig.class, CorsProperties.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========================================================================
    // Test-only controller scoped to this test class
    // ========================================================================
    @RestController
    @RequestMapping("/api/test")
    static class TestValidationController {

        record TestRequest(
                @NotBlank(message = "Email không được để trống")
                @Email(message = "Email không đúng định dạng hợp lệ")
                String email,

                @NotBlank(message = "Mật khẩu không được để trống")
                @Size(min = 8, max = 64, message = "Mật khẩu phải từ 8 đến 64 ký tự")
                String password,

                @NotBlank(message = "Họ tên không được để trống")
                String fullName
        ) {}

        record TestResponse(String message) {}

        @PostMapping("/validation")
        public TestResponse testValidation(@Valid @RequestBody TestRequest request) {
            return new TestResponse("Dữ liệu hợp lệ");
        }

        @GetMapping("/resource-not-found")
        public void testResourceNotFound() {
            throw new ResourceNotFoundException("Người dùng", "id", 999);
        }

        @GetMapping("/api-exception")
        public void testApiException() {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Email đã tồn tại trong hệ thống");
        }

        @GetMapping("/internal-error")
        public void testInternalError() {
            throw new RuntimeException("Unexpected internal error");
        }
    }

    // ========================================================================
    // Validation Tests
    // ========================================================================
    @Nested
    @DisplayName("Bean Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Valid request body returns 200 OK")
        void validRequest_Returns200() throws Exception {
            String validJson = """
                    {
                        "email": "test@example.com",
                        "password": "SecurePassword123!",
                        "fullName": "Nguyễn Văn A"
                    }
                    """;

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Dữ liệu hợp lệ"));
        }

        @Test
        @DisplayName("Missing required fields returns 400 with field-level validation errors")
        void missingFields_Returns400WithErrors() throws Exception {
            String invalidJson = """
                    {
                        "email": "",
                        "password": "",
                        "fullName": ""
                    }
                    """;

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                    .andExpect(jsonPath("$.path").value("/api/test/validation"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Invalid email format returns 400 with specific field error")
        void invalidEmail_Returns400WithFieldError() throws Exception {
            String invalidEmailJson = """
                    {
                        "email": "invalid-email",
                        "password": "SecurePassword123!",
                        "fullName": "Nguyễn Văn A"
                    }
                    """;

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidEmailJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field == 'email')].rejectedValue").value("invalid-email"))
                    .andExpect(jsonPath("$.errors[?(@.field == 'email')].reason").value("Email không đúng định dạng hợp lệ"));
        }

        @Test
        @DisplayName("Password too short returns 400 with size constraint error")
        void shortPassword_Returns400() throws Exception {
            String shortPwdJson = """
                    {
                        "email": "test@example.com",
                        "password": "abc",
                        "fullName": "Nguyễn Văn A"
                    }
                    """;

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(shortPwdJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
        }

        @Test
        @DisplayName("Malformed JSON body returns 400")
        void malformedJson_Returns400() throws Exception {
            String malformedJson = "{ invalid json }";

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/api/test/validation"));
        }
    }

    // ========================================================================
    // Custom Exception Handler Tests
    // ========================================================================
    @Nested
    @DisplayName("Custom Exception Handler Tests")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("ResourceNotFoundException returns 404 with ErrorResponse")
        void resourceNotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/test/resource-not-found")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Người dùng không tìm thấy với id: '999'"))
                    .andExpect(jsonPath("$.path").value("/api/test/resource-not-found"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("ApiException with CONFLICT returns 409 with ErrorResponse")
        void apiException_Returns409() throws Exception {
            mockMvc.perform(get("/api/test/api-exception")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
                    .andExpect(jsonPath("$.message").value("Email đã tồn tại trong hệ thống"))
                    .andExpect(jsonPath("$.path").value("/api/test/api-exception"));
        }

        @Test
        @DisplayName("Unhandled RuntimeException returns 500 Internal Server Error")
        void internalError_Returns500() throws Exception {
            mockMvc.perform(get("/api/test/internal-error")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống nội bộ. Vui lòng thử lại sau."))
                    .andExpect(jsonPath("$.path").value("/api/test/internal-error"));
        }
    }

    // ========================================================================
    // ErrorResponse Structure Verification
    // ========================================================================
    @Nested
    @DisplayName("ErrorResponse Structure Conformance")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Error response contains all required fields from API Contract 1.2")
        void errorResponse_ContainsAllRequiredFields() throws Exception {
            mockMvc.perform(get("/api/test/resource-not-found")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isString())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.path").isString());
        }

        @Test
        @DisplayName("Validation error response includes errors array with field/rejectedValue/reason")
        void validationErrorResponse_ContainsErrorsArray() throws Exception {
            String invalidJson = """
                    {
                        "email": "not-an-email",
                        "password": "abc",
                        "fullName": ""
                    }
                    """;

            mockMvc.perform(post("/api/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").isString())
                    .andExpect(jsonPath("$.errors[0].reason").isString());
        }
    }
}

package com.cnpm.careerrecommendation.common.exception;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandlerTest.TestValidationController.class)
@Import({SecurityConfig.class, CorsProperties.class, GlobalExceptionHandler.class})
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/api/test-handler")
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

        @GetMapping("/access-denied")
        public void testAccessDenied() {
            throw new AccessDeniedException("Forbidden action");
        }

        @GetMapping("/internal-error")
        public void testInternalError() {
            throw new RuntimeException("Unexpected internal error");
        }
    }

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

            mockMvc.perform(post("/api/test-handler/validation")
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

            mockMvc.perform(post("/api/test-handler/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Dữ liệu đầu vào không hợp lệ"))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/validation"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors").isNotEmpty())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Validation error does NOT leak rejected value (masked as null)")
        void validationError_DoesNotLeakRejectedValue() throws Exception {
            String invalidJson = """
                    {
                        "email": "not-an-email",
                        "password": "secret-plaintext-password",
                        "fullName": "Nguyễn Văn A"
                    }
                    """;

            mockMvc.perform(post("/api/test-handler/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors[0].field").value("email"))
                    .andExpect(jsonPath("$.errors[0].rejectedValue").value(nullValue()))
                    .andExpect(jsonPath("$.errors[0].reason").value("Email không đúng định dạng hợp lệ"));
        }

        @Test
        @DisplayName("Malformed JSON body returns 400")
        void malformedJson_Returns400() throws Exception {
            String malformedJson = "{ invalid json }";

            mockMvc.perform(post("/api/test-handler/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/validation"));
        }
    }

    @Nested
    @DisplayName("Custom Exception Handler Tests")
    class ExceptionHandlerTests {

        @Test
        @DisplayName("ResourceNotFoundException returns 404 with ErrorResponse")
        void resourceNotFound_Returns404() throws Exception {
            mockMvc.perform(get("/api/test-handler/resource-not-found")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Người dùng không tìm thấy với id: '999'"))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/resource-not-found"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("ApiException with CONFLICT returns 409 with ErrorResponse")
        void apiException_Returns409() throws Exception {
            mockMvc.perform(get("/api/test-handler/api-exception")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
                    .andExpect(jsonPath("$.message").value("Email đã tồn tại trong hệ thống"))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/api-exception"));
        }

        @Test
        @DisplayName("AccessDeniedException returns 403 Forbidden with ErrorResponse")
        void accessDenied_Returns403() throws Exception {
            mockMvc.perform(get("/api/test-handler/access-denied")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").value("Bạn không có quyền truy cập tài nguyên này"))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/access-denied"));
        }

        @Test
        @DisplayName("Unhandled RuntimeException returns 500 Internal Server Error")
        void internalError_Returns500() throws Exception {
            mockMvc.perform(get("/api/test-handler/internal-error")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("Đã xảy ra lỗi hệ thống"))
                    .andExpect(jsonPath("$.path").value("/api/test-handler/internal-error"));
        }
    }

    @Nested
    @DisplayName("ErrorResponse Structure Conformance")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Error response contains all required fields from API Contract 1.2")
        void errorResponse_ContainsAllRequiredFields() throws Exception {
            mockMvc.perform(get("/api/test-handler/resource-not-found")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isString())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.path").isString());
        }
    }
}

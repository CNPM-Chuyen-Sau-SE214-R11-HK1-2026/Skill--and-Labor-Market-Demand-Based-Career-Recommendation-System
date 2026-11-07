package com.cnpm.careerrecommendation.config;

import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.controller.HealthController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for CORS configuration verifying W1-T3 acceptance criteria:
 * - Allowed origins receive proper CORS headers
 * - Disallowed origins are rejected
 * - Preflight OPTIONS requests succeed for allowed origins
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, CorsProperties.class})
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecurityErrorHandler securityErrorHandler;

    @Test
    @DisplayName("CORS: Allowed origin receives Access-Control-Allow-Origin header")
    void allowedOrigin_ReceivesCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("CORS: Preflight OPTIONS request for allowed origin returns 200")
    void preflightRequest_AllowedOrigin_Returns200() throws Exception {
        mockMvc.perform(options("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @DisplayName("CORS: Disallowed origin does not receive Access-Control-Allow-Origin")
    void disallowedOrigin_Rejected() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://malicious-site.com"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("CORS: Credentials header is present when allowCredentials is true")
    void credentialsHeader_Present() throws Exception {
        mockMvc.perform(get("/api/health")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }
}

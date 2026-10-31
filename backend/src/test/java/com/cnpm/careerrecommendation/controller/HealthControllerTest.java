package com.cnpm.careerrecommendation.controller;

import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for GET /api/health endpoint verifying W1-T3 acceptance criteria:
 * - HTTP 200 response
 * - JSON body {"status":"UP"}
 * - Content-Type application/json
 */
@WebMvcTest(HealthController.class)
@Import({SecurityConfig.class, CorsProperties.class})
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health returns HTTP 200 with status UP")
    void healthCheck_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/health is publicly accessible without authentication")
    void healthCheck_NoAuthRequired() throws Exception {
        // No Authorization header provided — should still return 200
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("POST /api/health returns 405 Method Not Allowed with ErrorResponse")
    void healthCheck_PostMethodNotAllowed() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/health")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path").value("/api/health"));
    }
}

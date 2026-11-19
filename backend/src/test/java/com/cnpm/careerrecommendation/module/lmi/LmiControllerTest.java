package com.cnpm.careerrecommendation.module.lmi;

import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.lmi.controller.AdminLmiController;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportRequest;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportResponse;
import com.cnpm.careerrecommendation.module.lmi.service.LmiIngestionService;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminLmiController.class)
@Import({SecurityConfig.class, CorsProperties.class, SecurityErrorHandler.class, GlobalExceptionHandler.class})
class LmiControllerTest {

    private static final String IMPORT_ENDPOINT = "/api/v1/admin/lmi/import";
    private static final String VALID_ITEM_JSON = """
            {
              "occupationId": 101,
              "region": "HCMC",
              "demandScore": 88.50,
              "growthRatePct": 12.40,
              "openJobPostingsCount": 1250,
              "competitiveIndex": 2.80,
              "snapshotDate": "2026-11-01"
            }
            """;
    private static final String VALID_PAYLOAD = "{\"items\":[" + VALID_ITEM_JSON + "]}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LmiIngestionService lmiIngestionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /api/v1/admin/lmi/import without token returns 401 Unauthorized")
    void importMarketData_UnauthenticatedReturns401() throws Exception {
        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value(IMPORT_ENDPOINT));

        verify(lmiIngestionService, never()).importMarketData(any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("POST /api/v1/admin/lmi/import with ROLE_CANDIDATE returns 403 Forbidden")
    void importMarketData_CandidateRoleReturns403() throws Exception {
        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value(IMPORT_ENDPOINT));

        verify(lmiIngestionService, never()).importMarketData(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/lmi/import with ROLE_ADMIN returns 200 OK and summary counts")
    void importMarketData_AdminRoleReturns200() throws Exception {
        when(lmiIngestionService.importMarketData(any()))
                .thenReturn(new LmiImportResponse(1, 1, 0, Instant.parse("2026-11-19T12:00:00Z")));

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalRecordsProcessed").value(1))
                .andExpect(jsonPath("$.recordsInserted").value(1))
                .andExpect(jsonPath("$.recordsUpdated").value(0))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/lmi/import rejects demandScore > 100 or < 0 with 400 Bad Request")
    void importMarketData_InvalidDemandScoreReturns400() throws Exception {
        String overMaxPayload = """
                {
                  "items": [
                    {
                      "occupationId": 101,
                      "region": "NATIONAL",
                      "demandScore": 105.00,
                      "openJobPostingsCount": 10,
                      "snapshotDate": "2026-11-01"
                    }
                  ]
                }
                """;

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overMaxPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("items[0].demandScore"));

        String negativePayload = """
                {
                  "items": [
                    {
                      "occupationId": 101,
                      "demandScore": -1,
                      "openJobPostingsCount": -5,
                      "snapshotDate": "2026-11-01"
                    }
                  ]
                }
                """;

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(negativePayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());

        verify(lmiIngestionService, never()).importMarketData(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/lmi/import rejects fractional integer fields occupationId and openJobPostingsCount")
    void importMarketData_FractionalIntegersReturn400() throws Exception {
        String fractionalPayload = """
                {
                  "items": [
                    {
                      "occupationId": 101.5,
                      "demandScore": 80.00,
                      "openJobPostingsCount": 10.9,
                      "snapshotDate": "2026-11-01"
                    }
                  ]
                }
                """;

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fractionalPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors").isNotEmpty());

        verify(lmiIngestionService, never()).importMarketData(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/lmi/import rejects empty, oversized (>500), and unknown-property payloads")
    void importMarketData_EmptyOversizedAndUnknownPropertyReturn400() throws Exception {
        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("items"));

        String oversizedPayload = "{\"items\":["
                + String.join(",", Collections.nCopies(LmiImportRequest.MAX_BATCH_SIZE + 1, VALID_ITEM_JSON))
                + "]}";
        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("items"));

        String unknownFieldPayload = """
                {
                  "items": [
                    {
                      "occupationId": 101,
                      "demandScore": 80.00,
                      "snapshotDate": "2026-11-01",
                      "unexpectedField": "rejected"
                    }
                  ]
                }
                """;

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(unknownFieldPayload)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(lmiIngestionService, never()).importMarketData(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/lmi/import returns 400 INVALID_OCCUPATION when occupation does not exist")
    void importMarketData_UnknownOccupationReturns400() throws Exception {
        when(lmiIngestionService.importMarketData(any()))
                .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPATION",
                        "Không tìm thấy nghề nghiệp với occupationId=999999."));

        mockMvc.perform(post(IMPORT_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_OCCUPATION"));
    }
}

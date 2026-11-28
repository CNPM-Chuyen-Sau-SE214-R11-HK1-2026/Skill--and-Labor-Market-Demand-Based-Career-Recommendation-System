package com.cnpm.careerrecommendation.module.taxonomy;

import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.taxonomy.controller.AdminTaxonomyController;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminOccupationResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.AdminSkillResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationGroupResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSkillMappingResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillCategoryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.service.AdminTaxonomyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTaxonomyController.class)
@Import({SecurityConfig.class, CorsProperties.class, SecurityErrorHandler.class, GlobalExceptionHandler.class})
class AdminTaxonomyControllerTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-11-26T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminTaxonomyService adminTaxonomyService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Unauthenticated requests to /api/v1/admin/occupations* and /skills* return 401")
    void unauthenticatedRequestsReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/occupations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SOC-1\",\"title\":\"Dev\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/v1/admin/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SK-1\",\"name\":\"Java\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(delete("/api/v1/admin/occupations/1/skills/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(adminTaxonomyService, never()).createOccupation(any());
        verify(adminTaxonomyService, never()).createSkill(any());
        verify(adminTaxonomyService, never()).removeOccupationSkill(any(), any());
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    @DisplayName("ROLE_CANDIDATE requests to admin taxonomy endpoints return 403 Forbidden")
    void candidateRoleReturns403AcrossEndpoints() throws Exception {
        mockMvc.perform(post("/api/v1/admin/occupations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SOC-1\",\"title\":\"Dev\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(put("/api/v1/admin/skills/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SK-1\",\"name\":\"Java\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(post("/api/v1/admin/occupations/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":12,\"importanceWeight\":0.80,\"isRequired\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/v1/admin/occupation-groups"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(adminTaxonomyService, never()).createOccupation(any());
        verify(adminTaxonomyService, never()).updateSkill(any(), any());
        verify(adminTaxonomyService, never()).assignOccupationSkill(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Occupation CRUD endpoints return 201, 200, 204 and handle 409/404/400")
    void occupationCrudEndpoints() throws Exception {
        AdminOccupationResponse created = new AdminOccupationResponse(
                10L, "SOC-NEW", "AI Systems Engineer", 1L, "Data & AI",
                "Design AI systems", "IT", new BigDecimal("20000000.00"), new BigDecimal("45000000.00"),
                FIXED_TIME, FIXED_TIME);
        when(adminTaxonomyService.createOccupation(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/admin/occupations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "SOC-NEW",
                                  "title": "AI Systems Engineer",
                                  "groupId": 1,
                                  "description": "Design AI systems",
                                  "industry": "IT",
                                  "entrySalaryAvg": 20000000,
                                  "midSalaryAvg": 45000000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.code").value("SOC-NEW"))
                .andExpect(jsonPath("$.groupName").value("Data & AI"));

        when(adminTaxonomyService.updateOccupation(eq(10L), any())).thenReturn(created);
        mockMvc.perform(put("/api/v1/admin/occupations/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SOC-NEW\",\"title\":\"AI Systems Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        doNothing().when(adminTaxonomyService).deleteOccupation(10L);
        mockMvc.perform(delete("/api/v1/admin/occupations/10"))
                .andExpect(status().isNoContent());

        when(adminTaxonomyService.createOccupation(any())).thenThrow(
                new ApiException(HttpStatus.CONFLICT, "OCCUPATION_CODE_ALREADY_EXISTS", "Trùng mã nghề."));
        mockMvc.perform(post("/api/v1/admin/occupations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SOC-DUP\",\"title\":\"Dup\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("OCCUPATION_CODE_ALREADY_EXISTS"));

        doThrow(new ResourceNotFoundException("Nghề nghiệp", "id", 999L))
                .when(adminTaxonomyService).deleteOccupation(999L);
        mockMvc.perform(delete("/api/v1/admin/occupations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Skill CRUD endpoints return 201, 200, 204 and reject invalid requests")
    void skillCrudEndpoints() throws Exception {
        AdminSkillResponse created = new AdminSkillResponse(
                42L, "SK-RUST", "Rust", 1L, "Ngôn ngữ Lập trình",
                "TECHNICAL", "Systems programming", "ESCO", FIXED_TIME, FIXED_TIME);
        when(adminTaxonomyService.createSkill(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/admin/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SK-RUST\",\"name\":\"Rust\",\"categoryId\":1,\"skillType\":\"TECHNICAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.code").value("SK-RUST"))
                .andExpect(jsonPath("$.standardSource").value("ESCO"));

        when(adminTaxonomyService.updateSkill(eq(42L), any())).thenReturn(created);
        mockMvc.perform(put("/api/v1/admin/skills/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SK-RUST\",\"name\":\"Rust\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));

        doNothing().when(adminTaxonomyService).deleteSkill(42L);
        mockMvc.perform(delete("/api/v1/admin/skills/42"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/admin/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\",\"name\":\"Rust\",\"unknownField\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Occupation-Skill mapping endpoints support assign/upsert, update, delete by path & query, and validate weight")
    void occupationSkillMappingEndpoints() throws Exception {
        OccupationSkillMappingResponse mapping = new OccupationSkillMappingResponse(
                7L, 10L, "SOC-NEW", "AI Engineer",
                42L, "SK-RUST", "Rust",
                new BigDecimal("0.85"), true, FIXED_TIME, FIXED_TIME);

        when(adminTaxonomyService.assignOccupationSkill(eq(10L), any()))
                .thenReturn(new AdminTaxonomyService.OccupationSkillAssignment(mapping, true))
                .thenReturn(new AdminTaxonomyService.OccupationSkillAssignment(mapping, false));

        mockMvc.perform(post("/api/v1/admin/occupations/10/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":42,\"importanceWeight\":0.85,\"isRequired\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.occupationId").value(10))
                .andExpect(jsonPath("$.skillId").value(42))
                .andExpect(jsonPath("$.importanceWeight").value(0.85));

        mockMvc.perform(post("/api/v1/admin/occupations/10/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":42,\"importanceWeight\":0.85,\"isRequired\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        when(adminTaxonomyService.updateOccupationSkill(eq(10L), eq(42L), any())).thenReturn(mapping);
        mockMvc.perform(put("/api/v1/admin/occupations/10/skills/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"importanceWeight\":0.85,\"isRequired\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skillCode").value("SK-RUST"));

        doNothing().when(adminTaxonomyService).removeOccupationSkill(10L, 42L);
        mockMvc.perform(delete("/api/v1/admin/occupations/10/skills/42"))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/admin/occupations/10/skills").param("skillId", "42"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Invalid importanceWeight or fractional skillId rejects with 400 and never calls service")
    void invalidWeightAndFractionalSkillIdReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/admin/occupations/10/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":42,\"importanceWeight\":1.25,\"isRequired\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        mockMvc.perform(post("/api/v1/admin/occupations/10/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillId\":42.5,\"importanceWeight\":0.50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(adminTaxonomyService, never()).assignOccupationSkill(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Invalid path variables, non-positive IDs, missing skillId query param, and admin metadata endpoints")
    void pathValidationAndMetadataEndpoints() throws Exception {
        mockMvc.perform(put("/api/v1/admin/occupations/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"SOC-1\",\"title\":\"Dev\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));

        mockMvc.perform(delete("/api/v1/admin/skills/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));

        mockMvc.perform(delete("/api/v1/admin/occupations/10/skills"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));

        when(adminTaxonomyService.listOccupationGroups())
                .thenReturn(List.of(new OccupationGroupResponse(1L, "GRP-SWE", "SWE", "Desc")));
        when(adminTaxonomyService.listSkillCategories())
                .thenReturn(List.of(new SkillCategoryResponse(1L, "CAT-PROG", "Programming", "Desc")));
        when(adminTaxonomyService.listOccupationSkills(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/occupation-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("GRP-SWE"));

        mockMvc.perform(get("/api/v1/admin/skill-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT-PROG"));

        mockMvc.perform(get("/api/v1/admin/occupations/10/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

package com.cnpm.careerrecommendation.module.taxonomy;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.taxonomy.controller.TaxonomyController;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationDetailResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSummaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillDictionaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.service.TaxonomyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaxonomyController.class)
@Import({SecurityConfig.class, CorsProperties.class, SecurityErrorHandler.class, GlobalExceptionHandler.class})
class TaxonomyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaxonomyService taxonomyService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/v1/occupations returns 200 OK with PageResponse and is publicly accessible")
    void searchOccupations_PublicAccessOk() throws Exception {
        OccupationSummaryResponse summary = new OccupationSummaryResponse(
            101L,
            "SOC-15-1252",
            "Software Developer / Kỹ sư phần mềm",
            "Công nghệ thông tin & Khoa học máy tính",
            new BigDecimal("15000000"),
            new BigDecimal("35000000"),
            new BigDecimal("88.5"),
            new BigDecimal("12.4")
        );

        PageResponse<OccupationSummaryResponse> pageResponse = PageResponse.of(
            List.of(summary), 0, 20, 1, 1
        );

        when(taxonomyService.searchOccupations(any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/occupations")
                .param("page", "0")
                .param("size", "20")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(101))
            .andExpect(jsonPath("$.content[0].code").value("SOC-15-1252"))
            .andExpect(jsonPath("$.content[0].title").value("Software Developer / Kỹ sư phần mềm"))
            .andExpect(jsonPath("$.content[0].groupName").value("Công nghệ thông tin & Khoa học máy tính"))
            .andExpect(jsonPath("$.content[0].demandScore").value(88.5))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    @DisplayName("GET /api/v1/occupations/{id} returns 200 OK with detailed information")
    void getOccupationDetail_Success() throws Exception {
        OccupationDetailResponse.SalaryRangeDto salary = new OccupationDetailResponse.SalaryRangeDto(
            new BigDecimal("15000000"), new BigDecimal("35000000"), "VND"
        );
        OccupationDetailResponse.MarketIndicatorDto lmi = new OccupationDetailResponse.MarketIndicatorDto(
            new BigDecimal("88.5"), new BigDecimal("12.4"), "HIGH", 1250, "2025-2026", "Cục Thống kê & Báo cáo WEF 2025"
        );
        OccupationDetailResponse.OccupationSkillRequirementDto skill = new OccupationDetailResponse.OccupationSkillRequirementDto(
            12L, "SK-JAVA-01", "Java", "ESSENTIAL", 1.0
        );

        OccupationDetailResponse detail = new OccupationDetailResponse(
            101L, "SOC-15-1252", "Software Developer / Kỹ sư phần mềm",
            "Công nghệ thông tin", "Mô tả nghề", salary, lmi, List.of(skill)
        );

        when(taxonomyService.getOccupationDetail(101L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/occupations/101")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(101))
            .andExpect(jsonPath("$.code").value("SOC-15-1252"))
            .andExpect(jsonPath("$.salaryRange.currency").value("VND"))
            .andExpect(jsonPath("$.marketIndicators.shortageLevel").value("HIGH"))
            .andExpect(jsonPath("$.skills[0].skillName").value("Java"))
            .andExpect(jsonPath("$.skills[0].importanceType").value("ESSENTIAL"));
    }

    @Test
    @DisplayName("GET /api/v1/occupations/{id} when not found returns 404 with standardized ErrorResponse")
    void getOccupationDetail_NotFoundReturns404() throws Exception {
        when(taxonomyService.getOccupationDetail(999L))
            .thenThrow(new ResourceNotFoundException("Nghề nghiệp", "id", 999L));

        mockMvc.perform(get("/api/v1/occupations/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/skills returns 200 OK with skills list and is publicly accessible")
    void searchSkills_PublicAccessOk() throws Exception {
        SkillDictionaryResponse skill = new SkillDictionaryResponse(
            12L, "SK-JAVA-01", "Java", "Lập trình", "ESCO"
        );
        when(taxonomyService.searchSkills(any(), any())).thenReturn(List.of(skill));

        mockMvc.perform(get("/api/v1/skills")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(12))
            .andExpect(jsonPath("$[0].code").value("SK-JAVA-01"))
            .andExpect(jsonPath("$[0].name").value("Java"))
            .andExpect(jsonPath("$[0].standardSource").value("ESCO"));
    }

    @Test
    @DisplayName("GET /api/v1/skills with short query returns 400 Bad Request")
    void searchSkills_ShortQueryReturns400() throws Exception {
        when(taxonomyService.searchSkills(eq("a"), any()))
            .thenThrow(new ApiException(HttpStatus.BAD_REQUEST, "INVALID_QUERY", "Từ khóa tìm kiếm phải có tối thiểu 2 ký tự"));

        mockMvc.perform(get("/api/v1/skills")
                .param("query", "a")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_QUERY"))
            .andExpect(jsonPath("$.status").value(400));
    }
}

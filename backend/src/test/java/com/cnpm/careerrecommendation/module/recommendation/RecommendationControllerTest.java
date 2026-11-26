package com.cnpm.careerrecommendation.module.recommendation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Role;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.GlobalExceptionHandler;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.common.security.AuthenticatedUser;
import com.cnpm.careerrecommendation.common.security.JwtTokenProvider;
import com.cnpm.careerrecommendation.common.security.SecurityErrorHandler;
import com.cnpm.careerrecommendation.config.CorsProperties;
import com.cnpm.careerrecommendation.config.SecurityConfig;
import com.cnpm.careerrecommendation.module.recommendation.controller.RecommendationController;
import com.cnpm.careerrecommendation.module.recommendation.dto.*;
import com.cnpm.careerrecommendation.module.recommendation.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.MalformedJwtException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@Import({
  SecurityConfig.class,
  CorsProperties.class,
  SecurityErrorHandler.class,
  GlobalExceptionHandler.class
})
class RecommendationControllerTest {
  @Autowired MockMvc mvc;
  @MockBean RecommendationService service;
  @MockBean JwtTokenProvider jwt;
  @MockBean UserRepository users;

  @Test
  void candidateGetsRecommendations() throws Exception {
    var item =
        new RecommendationResponse(
            1,
            4L,
            "C4",
            "Analyst",
            null,
            87.5,
            new ScoreBreakdownResponse(
                90, 80, 70, "0.6 * SkillMatch + 0.3 * DemandScore + 0.1 * PrefMatch"),
            "+12.5%",
            2,
            3,
            "2 skills matched");
    when(service.getRecommendations(anyLong(), anyInt())).thenReturn(List.of(item));
    mvc.perform(get("/api/v1/recommendations").with(candidate()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rank").value(1))
        .andExpect(jsonPath("$[0].occupationId").value(4))
        .andExpect(jsonPath("$[0].finalScore").value(87.5))
        .andExpect(jsonPath("$[0].scoreBreakdown.skillMatchPct").value(90))
        .andExpect(jsonPath("$[0].scoreBreakdown.demandScore").value(80))
        .andExpect(jsonPath("$[0].scoreBreakdown.preferenceMatchPct").value(70))
        .andExpect(jsonPath("$[0].marketGrowth").value("+12.5%"))
        .andExpect(jsonPath("$[0].matchedSkillsCount").value(2))
        .andExpect(jsonPath("$[0].totalRequiredSkillsCount").value(3));
  }

  @Test
  void recommendationsRequireCandidateRole() throws Exception {
    mvc.perform(get("/api/v1/recommendations")).andExpect(status().isUnauthorized());
    mvc.perform(
            get("/api/v1/recommendations")
                .with(
                    authentication(
                        new UsernamePasswordAuthenticationToken(
                            new AuthenticatedUser(7L, "admin", Role.ROLE_ADMIN),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
        .andExpect(status().isForbidden());
  }

  @Test
  void noSkillsReturnsBadRequest() throws Exception {
    when(service.getRecommendations(anyLong(), anyInt()))
        .thenThrow(
            new ApiException(
                HttpStatus.BAD_REQUEST, "NO_SKILLS_DECLARED", "Please add at least one skill."));
    mvc.perform(get("/api/v1/recommendations").with(candidate()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("NO_SKILLS_DECLARED"));
  }

  @Test
  void candidateGetsSkillGap() throws Exception {
    when(service.getSkillGap(anyLong(), eq(4L)))
        .thenReturn(
            new SkillGapResponse(
                4L,
                "Analyst",
                50,
                List.of(new MatchedSkillResponse(1L, "Java", "ADVANCED", "HIGH", BigDecimal.ONE)),
                List.of(
                    new MissingSkillResponse(
                        2L, "SQL", "HIGH", BigDecimal.ONE, "CRITICAL", "Learn SQL")),
                List.of(
                    new MissingSkillResponse(
                        3L, "Git", "MEDIUM", new BigDecimal(".5"), "MEDIUM", "Learn Git"))));
    mvc.perform(get("/api/v1/recommendations/4/skill-gap").with(candidate()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.matchedSkills").isArray())
        .andExpect(jsonPath("$.missingEssentialSkills[0].priority").value("CRITICAL"))
        .andExpect(jsonPath("$.missingOptionalSkills[0].name").value("Git"));
  }

  @Test
  void missingOccupationReturnsNotFound() throws Exception {
    when(service.getSkillGap(anyLong(), eq(44L)))
        .thenThrow(new ResourceNotFoundException("Occupation", "id", 44L));
    mvc.perform(get("/api/v1/recommendations/44/skill-gap").with(candidate()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void candidateGetsPaginatedHistory() throws Exception {
    var snapshot = new ObjectMapper().readTree("{\"limit\":10,\"userSkillIds\":[1]}");
    var item =
        new RecommendationHistoryItemResponse(
            55L,
            101L,
            "SOC-15-1252",
            "Software Developer",
            "IT",
            new BigDecimal("88.50"),
            2,
            Instant.parse("2026-11-25T10:15:30Z"),
            Instant.parse("2026-11-25T10:15:31Z"),
            snapshot);
    when(service.getRecommendationHistory(7L, 0, 20))
        .thenReturn(PageResponse.of(List.of(item), 0, 20, 1, 1));

    mvc.perform(get("/api/v1/recommendations/history").with(candidate()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(55))
        .andExpect(jsonPath("$.content[0].occupationId").value(101))
        .andExpect(jsonPath("$.content[0].occupationCode").value("SOC-15-1252"))
        .andExpect(jsonPath("$.content[0].occupationTitle").value("Software Developer"))
        .andExpect(jsonPath("$.content[0].groupName").value("IT"))
        .andExpect(jsonPath("$.content[0].matchScore").value(88.5))
        .andExpect(jsonPath("$.content[0].skillGapCount").value(2))
        .andExpect(jsonPath("$.content[0].recommendationDate").value("2026-11-25T10:15:30Z"))
        .andExpect(jsonPath("$.content[0].createdAt").value("2026-11-25T10:15:31Z"))
        .andExpect(jsonPath("$.content[0].inputSnapshot.limit").value(10))
        .andExpect(jsonPath("$.pageNumber").value(0))
        .andExpect(jsonPath("$.pageSize").value(20))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.last").value(true));

    verify(service).getRecommendationHistory(7L, 0, 20);
  }

  @Test
  void candidateGetsEmptyHistoryPage() throws Exception {
    when(service.getRecommendationHistory(7L, 0, 20))
        .thenReturn(PageResponse.of(List.of(), 0, 20, 0, 0));

    mvc.perform(get("/api/v1/recommendations/history").with(candidate()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0))
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  void historyForwardsCustomPagination() throws Exception {
    when(service.getRecommendationHistory(7L, 1, 5))
        .thenReturn(PageResponse.of(List.of(), 1, 5, 0, 0));

    mvc.perform(
            get("/api/v1/recommendations/history")
                .param("page", "1")
                .param("size", "5")
                .with(candidate()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pageNumber").value(1))
        .andExpect(jsonPath("$.pageSize").value(5));

    verify(service).getRecommendationHistory(7L, 1, 5);
  }

  @Test
  void historyIgnoresUserIdQueryParamToPreventIdor() throws Exception {
    when(service.getRecommendationHistory(7L, 0, 20))
        .thenReturn(PageResponse.of(List.of(), 0, 20, 0, 0));

    mvc.perform(
            get("/api/v1/recommendations/history")
                .param("userId", "999")
                .with(candidate()))
        .andExpect(status().isOk());

    verify(service).getRecommendationHistory(7L, 0, 20);
    verify(service, never()).getRecommendationHistory(eq(999L), anyInt(), anyInt());
  }

  @Test
  void historyRequiresCandidateRole() throws Exception {
    mvc.perform(get("/api/v1/recommendations/history"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));

    when(jwt.parseToken("invalid-token")).thenThrow(new MalformedJwtException("invalid"));
    mvc.perform(
            get("/api/v1/recommendations/history")
                .header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));

    mvc.perform(
            get("/api/v1/recommendations/history")
                .with(
                    authentication(
                        new UsernamePasswordAuthenticationToken(
                            new AuthenticatedUser(8L, "admin", Role.ROLE_ADMIN),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403));

    verifyNoInteractions(service);
  }

  @Test
  void historyRejectsInvalidPagination() throws Exception {
    when(service.getRecommendationHistory(7L, -1, 20))
        .thenThrow(
            new ApiException(
                HttpStatus.BAD_REQUEST,
                "INVALID_PAGINATION",
                "Page must be >= 0 and size must be between 1 and 100."));

    mvc.perform(
            get("/api/v1/recommendations/history")
                .param("page", "-1")
                .with(candidate()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("INVALID_PAGINATION"));

    mvc.perform(
            get("/api/v1/recommendations/history")
                .param("page", "abc")
                .with(candidate()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));

    verify(service).getRecommendationHistory(7L, -1, 20);
    verifyNoMoreInteractions(service);
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor candidate() {
    return authentication(
        new UsernamePasswordAuthenticationToken(
            new AuthenticatedUser(7L, "candidate", Role.ROLE_CANDIDATE),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))));
  }
}

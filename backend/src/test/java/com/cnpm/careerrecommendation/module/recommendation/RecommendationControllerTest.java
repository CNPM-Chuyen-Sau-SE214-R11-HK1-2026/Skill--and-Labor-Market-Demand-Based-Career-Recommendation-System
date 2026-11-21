package com.cnpm.careerrecommendation.module.recommendation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import java.math.BigDecimal;
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

  private org.springframework.test.web.servlet.request.RequestPostProcessor candidate() {
    return authentication(
        new UsernamePasswordAuthenticationToken(
            new AuthenticatedUser(7L, "candidate", Role.ROLE_CANDIDATE),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))));
  }
}

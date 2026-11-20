package com.cnpm.careerrecommendation.module.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.entity.*;
import com.cnpm.careerrecommendation.module.recommendation.service.*;
import com.cnpm.careerrecommendation.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {
  @Mock UserSkillRepository userskills;
  @Mock CandidateProfileRepository profiles;
  @Mock OccupationRepository occupations;
  @Mock OccupationSkillRepository occSkills;
  @Mock MarketDemandIndicatorRepository indicators;
  @Mock RecommendationLogRepository logs;
  @Mock UserRepository users;
  private RecommendationServiceImpl service;
  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    service =
        new RecommendationServiceImpl(
            new RecommendationEngine(),
            userskills,
            profiles,
            occupations,
            occSkills,
            indicators,
            logs,
            users,
            mapper);
  }

  @Test
  @DisplayName("Recommendations batch-fetch, rank Top-N, and save exactly one JSON audit log")
  void recommendations() throws Exception {
    Skill s = skill(1L, "Java");
    UserSkill us = userSkill(s);
    Occupation low = occ(2L, "Low"), high = occ(1L, "High");
    when(userskills.findByUserId(7L)).thenReturn(List.of(us));
    when(profiles.findByUserId(7L)).thenReturn(Optional.empty());
    when(occupations.findAll()).thenReturn(List.of(low, high));
    when(occSkills.findAll())
        .thenReturn(
            List.of(
                new OccupationSkill(low, s, BigDecimal.ONE, true),
                new OccupationSkill(high, s, BigDecimal.ONE, true)));
    when(indicators.findLatestByOccupationIds(List.of(2L, 1L)))
        .thenReturn(
            List.of(
                new MarketDemandIndicator(low, "NATIONAL", new BigDecimal("20"), LocalDate.now()),
                new MarketDemandIndicator(
                    high, "NATIONAL", new BigDecimal("90"), LocalDate.now())));
    when(users.getReferenceById(7L)).thenReturn(null);
    var result = service.getRecommendations(7L, 10);
    assertEquals(1L, result.get(0).occupationId());
    assertEquals(92.0, result.get(0).finalScore());
    verify(userskills).findByUserId(7L);
    verify(occSkills).findAll();
    verify(indicators).findLatestByOccupationIds(List.of(2L, 1L));
    verify(occSkills, never()).findByOccupationId(anyLong());
    var captor = org.mockito.ArgumentCaptor.forClass(RecommendationLog.class);
    verify(logs, times(1)).save(captor.capture());
    assertEquals(high, captor.getValue().getOccupation());
    assertEquals(new BigDecimal("92.00"), captor.getValue().getMatchScore());
    JsonNode json = mapper.readTree(captor.getValue().getInputSnapshotJson());
    assertEquals(10, json.get("limit").asInt());
    assertEquals(1, json.get("topRecommendations").get(0).get("occupationId").asLong());
  }

  @Test
  @DisplayName("No declared skills raises bad request without audit log")
  void noSkills() {
    when(userskills.findByUserId(7L)).thenReturn(List.of());
    ApiException e = assertThrows(ApiException.class, () -> service.getRecommendations(7L, 10));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    assertEquals("NO_SKILLS_DECLARED", e.getErrorCode());
    verifyNoInteractions(logs);
  }

  @Test
  @DisplayName("Limits outside one through fifty are rejected")
  void invalidLimits() {
    for (int limit : List.of(0, 51)) {
      ApiException e =
          assertThrows(ApiException.class, () -> service.getRecommendations(7L, limit));
      assertEquals("INVALID_LIMIT", e.getErrorCode());
    }
    verifyNoInteractions(userskills, logs);
  }

  @Test
  @DisplayName("Skill gap partitions required and optional skills and computes percentage")
  void skillGap() {
    Skill a = skill(1L, "Java"), b = skill(2L, "SQL"), c = skill(3L, "Git");
    Occupation o = occ(1L, "Engineer");
    when(occupations.findById(1L)).thenReturn(Optional.of(o));
    when(userskills.findByUserId(7L)).thenReturn(List.of(userSkill(a)));
    when(occSkills.findByOccupationIdWithSkills(1L))
        .thenReturn(
            List.of(
                new OccupationSkill(o, a, new BigDecimal("1.00"), true),
                new OccupationSkill(o, b, new BigDecimal("0.80"), true),
                new OccupationSkill(o, c, new BigDecimal("0.40"), false)));
    var gap = service.getSkillGap(7L, 1L);
    assertEquals(45.5, gap.matchPercentage());
    assertEquals(1, gap.matchedSkills().size());
    assertEquals(1, gap.missingEssentialSkills().size());
    assertEquals("CRITICAL", gap.missingEssentialSkills().get(0).priority());
    assertEquals(1, gap.missingOptionalSkills().size());
  }

  @Test
  @DisplayName("Missing occupation raises not found")
  void occupationMissing() {
    when(occupations.findById(4L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.getSkillGap(7L, 4L));
  }

  @Test
  @DisplayName("No user skills yields zero match and all occupation skills missing")
  void emptyGap() {
    Skill s = skill(1L, "Java");
    Occupation o = occ(1L, "Engineer");
    when(occupations.findById(1L)).thenReturn(Optional.of(o));
    when(userskills.findByUserId(7L)).thenReturn(List.of());
    when(occSkills.findByOccupationIdWithSkills(1L))
        .thenReturn(List.of(new OccupationSkill(o, s, BigDecimal.ONE, true)));
    var gap = service.getSkillGap(7L, 1L);
    assertEquals(0, gap.matchPercentage());
    assertTrue(gap.matchedSkills().isEmpty());
    assertEquals(1, gap.missingEssentialSkills().size());
  }

  private static Skill skill(Long id, String name) {
    Skill s = new Skill(name, name, "TECHNICAL", null);
    s.setId(id);
    return s;
  }

  private static UserSkill userSkill(Skill skill) {
    UserSkill u = new UserSkill();
    u.setSkill(skill);
    u.setProficiencyLevel("EXPERT");
    return u;
  }

  private static Occupation occ(Long id, String title) {
    Occupation o = new Occupation("C" + id, title, null, null);
    o.setId(id);
    return o;
  }
}

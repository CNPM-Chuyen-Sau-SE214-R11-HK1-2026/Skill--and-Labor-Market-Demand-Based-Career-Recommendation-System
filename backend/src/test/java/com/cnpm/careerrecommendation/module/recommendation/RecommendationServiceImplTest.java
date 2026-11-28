package com.cnpm.careerrecommendation.module.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.entity.*;
import com.cnpm.careerrecommendation.module.recommendation.dto.RecommendationHistoryItemResponse;
import com.cnpm.careerrecommendation.module.recommendation.service.*;
import com.cnpm.careerrecommendation.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

  @Test
  @DisplayName("History queries newest-first paginated logs and parses JSON snapshot")
  void historyReturnsNewestFirstPageMappedFromLogs() {
    OccupationGroup group = new OccupationGroup("15-0000", "Information Technology", null);
    Occupation o = new Occupation("SOC-15-1252", "Software Developer", "IT", group);
    o.setId(101L);
    OffsetDateTime recDate = OffsetDateTime.parse("2026-11-25T10:15:30Z");
    OffsetDateTime created = OffsetDateTime.parse("2026-11-25T10:15:31Z");
    RecommendationLog log =
        new RecommendationLog(
            null,
            o,
            new BigDecimal("88.50"),
            2,
            "{\"limit\":10,\"userSkillIds\":[1,2]}");
    log.setId(55L);
    log.setRecommendationDate(recDate);
    log.setCreatedAt(created);

    when(logs.findHistoryByUserId(eq(7L), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1));

    PageResponse<RecommendationHistoryItemResponse> page =
        service.getRecommendationHistory(7L, 0, 20);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(logs).findHistoryByUserId(eq(7L), pageableCaptor.capture());
    Pageable captured = pageableCaptor.getValue();
    assertEquals(0, captured.getPageNumber());
    assertEquals(20, captured.getPageSize());
    assertEquals(
        List.of(Sort.Order.desc("recommendationDate"), Sort.Order.desc("id")),
        captured.getSort().toList());

    assertEquals(1, page.content().size());
    assertEquals(0, page.pageNumber());
    assertEquals(20, page.pageSize());
    assertEquals(1L, page.totalElements());
    assertEquals(1, page.totalPages());
    assertTrue(page.last());

    RecommendationHistoryItemResponse item = page.content().get(0);
    assertEquals(55L, item.id());
    assertEquals(101L, item.occupationId());
    assertEquals("SOC-15-1252", item.occupationCode());
    assertEquals("Software Developer", item.occupationTitle());
    assertEquals("Information Technology", item.groupName());
    assertEquals(new BigDecimal("88.50"), item.matchScore());
    assertEquals(2, item.skillGapCount());
    assertEquals(Instant.parse("2026-11-25T10:15:30Z"), item.recommendationDate());
    assertEquals(Instant.parse("2026-11-25T10:15:31Z"), item.createdAt());
    assertNotNull(item.inputSnapshot());
    assertEquals(10, item.inputSnapshot().get("limit").asInt());
    assertEquals(1, item.inputSnapshot().get("userSkillIds").get(0).asInt());
  }

  @Test
  @DisplayName("History safely handles null group, null/blank snapshot, and malformed JSON")
  void historyHandlesNullGroupNullSnapshotAndMalformedSnapshot() {
    Occupation o = occ(102L, "Systems Analyst");
    RecommendationLog nullSnapshot =
        new RecommendationLog(null, o, new BigDecimal("70.00"), 1, null);
    nullSnapshot.setId(1L);
    RecommendationLog blankSnapshot =
        new RecommendationLog(null, o, new BigDecimal("75.00"), null, "   ");
    blankSnapshot.setId(2L);
    blankSnapshot.setSkillGapCount(null);
    RecommendationLog malformedSnapshot =
        new RecommendationLog(null, o, new BigDecimal("80.00"), 0, "{not-valid-json");
    malformedSnapshot.setId(3L);

    when(logs.findHistoryByUserId(eq(7L), any(Pageable.class)))
        .thenReturn(
            new PageImpl<>(
                List.of(nullSnapshot, blankSnapshot, malformedSnapshot),
                PageRequest.of(0, 20),
                3));

    PageResponse<RecommendationHistoryItemResponse> page =
        service.getRecommendationHistory(7L, 0, 20);

    assertEquals(3, page.content().size());
    assertNull(page.content().get(0).groupName());
    assertNull(page.content().get(0).inputSnapshot());
    assertEquals(0, page.content().get(1).skillGapCount());
    assertNull(page.content().get(1).inputSnapshot());
    assertNull(page.content().get(2).inputSnapshot());
  }

  @Test
  @DisplayName("History returns empty PageResponse when candidate has no logs")
  void historyReturnsEmptyPageWhenNoLogsExist() {
    when(logs.findHistoryByUserId(eq(7L), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

    PageResponse<RecommendationHistoryItemResponse> page =
        service.getRecommendationHistory(7L, 0, 20);

    assertTrue(page.content().isEmpty());
    assertEquals(0L, page.totalElements());
    assertEquals(0, page.totalPages());
    assertTrue(page.last());
  }

  @Test
  @DisplayName("History rejects invalid page, size bounds, and offset overflow before querying repository")
  void historyRejectsInvalidPagination() {
    for (int[] pair : new int[][] {{-1, 20}, {0, 0}, {0, 101}, {107374183, 20}}) {
      ApiException ex =
          assertThrows(
              ApiException.class,
              () -> service.getRecommendationHistory(7L, pair[0], pair[1]));
      assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
      assertEquals("INVALID_PAGINATION", ex.getErrorCode());
    }
    verifyNoInteractions(logs);
  }

  @Test
  @DisplayName("History rejects null userId before querying repository")
  void historyRejectsNullUserId() {
    ApiException ex =
        assertThrows(
            ApiException.class, () -> service.getRecommendationHistory(null, 0, 20));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    assertEquals("UNAUTHORIZED", ex.getErrorCode());
    verifyNoInteractions(logs);
  }

  @Test
  @DisplayName("History queries strictly scope by authenticated userId")
  void historyScopesStrictlyToRequestedUserId() {
    when(logs.findHistoryByUserId(anyLong(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

    service.getRecommendationHistory(7L, 0, 10);
    service.getRecommendationHistory(8L, 1, 5);

    verify(logs).findHistoryByUserId(eq(7L), any(Pageable.class));
    verify(logs).findHistoryByUserId(eq(8L), any(Pageable.class));
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

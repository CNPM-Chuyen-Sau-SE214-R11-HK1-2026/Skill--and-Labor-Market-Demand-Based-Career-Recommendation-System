package com.cnpm.careerrecommendation.module.recommendation.service;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.common.exception.ResourceNotFoundException;
import com.cnpm.careerrecommendation.common.repository.UserRepository;
import com.cnpm.careerrecommendation.entity.*;
import com.cnpm.careerrecommendation.module.recommendation.dto.*;
import com.cnpm.careerrecommendation.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationServiceImpl implements RecommendationService {
  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
  private static final String FORMULA = "0.6 * SkillMatch + 0.3 * DemandScore + 0.1 * PrefMatch";
  private static final int MAX_HISTORY_PAGE_SIZE = 100;
  private static final Sort HISTORY_SORT =
      Sort.by(Sort.Order.desc("recommendationDate"), Sort.Order.desc("id"));
  private final RecommendationEngine engine;
  private final UserSkillRepository userSkills;
  private final CandidateProfileRepository profiles;
  private final OccupationRepository occupations;
  private final OccupationSkillRepository occupationSkills;
  private final MarketDemandIndicatorRepository indicators;
  private final RecommendationLogRepository logs;
  private final UserRepository users;
  private final ObjectMapper mapper;

  public RecommendationServiceImpl(
      RecommendationEngine engine,
      UserSkillRepository userSkills,
      CandidateProfileRepository profiles,
      OccupationRepository occupations,
      OccupationSkillRepository occupationSkills,
      MarketDemandIndicatorRepository indicators,
      RecommendationLogRepository logs,
      UserRepository users,
      ObjectMapper mapper) {
    this.engine = engine;
    this.userSkills = userSkills;
    this.profiles = profiles;
    this.occupations = occupations;
    this.occupationSkills = occupationSkills;
    this.indicators = indicators;
    this.logs = logs;
    this.users = users;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public List<RecommendationResponse> getRecommendations(Long userId, int limit) {
    validateLimit(limit);
    List<UserSkill> mine = userSkills.findByUserId(userId);
    if (mine.isEmpty())
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "NO_SKILLS_DECLARED",
          "Please add at least one skill to your profile before requesting recommendations.");
    CandidateProfile profile = profiles.findByUserId(userId).orElse(null);
    List<Occupation> all = occupations.findAll();
    if (all.isEmpty()) return List.of();
    List<Long> ids = all.stream().map(Occupation::getId).toList();
    Map<Long, List<OccupationSkill>> byOccupation =
        occupationSkills.findAll().stream()
            .collect(Collectors.groupingBy(s -> s.getOccupation().getId()));
    Map<Long, List<MarketDemandIndicator>> byIndicator =
        indicators.findLatestByOccupationIds(ids).stream()
            .collect(Collectors.groupingBy(i -> i.getOccupation().getId()));
    List<RecommendationEngine.Ranked> ranked =
        all.stream()
            .map(
                o -> {
                  List<OccupationSkill> skills = byOccupation.getOrDefault(o.getId(), List.of());
                  return new RecommendationEngine.Ranked(
                      o,
                      engine.score(
                          o, mine, skills, profile, byIndicator.getOrDefault(o.getId(), List.of())),
                      skills);
                })
            .sorted(RecommendationEngine.rankingOrder())
            .limit(limit)
            .toList();
    List<RecommendationResponse> result = new ArrayList<>();
    int rank = 1;
    for (var r : ranked) {
      var o = r.occupation();
      var score = r.score();
      long matched =
          r.skills().stream()
              .filter(
                  s ->
                      mine.stream()
                          .anyMatch(
                              u -> Objects.equals(u.getSkill().getId(), s.getSkill().getId())))
              .count();
      String group = o.getGroup() == null ? null : o.getGroup().getName();
      String growth =
          r.score().indicator() == null || r.score().indicator().getGrowthRatePct() == null
              ? "N/A"
              : (r.score().indicator().getGrowthRatePct().signum() > 0 ? "+" : "")
                  + RecommendationEngine.round1(
                      r.score().indicator().getGrowthRatePct().doubleValue())
                  + "%";
      result.add(
          new RecommendationResponse(
              rank++,
              o.getId(),
              o.getCode(),
              o.getTitle(),
              group,
              RecommendationEngine.round1(score.finalScore()),
              new ScoreBreakdownResponse(
                  RecommendationEngine.round1(score.skillMatchPct()),
                  RecommendationEngine.round1(score.demandScore()),
                  RecommendationEngine.round1(score.preferenceMatchPct()),
                  FORMULA),
              growth,
              (int) matched,
              r.skills().size(),
              matched
                  + " of "
                  + r.skills().size()
                  + " required skills matched; demand and preference fit: "
                  + RecommendationEngine.round1(score.demandScore())
                  + " and "
                  + RecommendationEngine.round1(score.preferenceMatchPct())
                  + "."));
    }
    if (!ranked.isEmpty()) saveLog(userId, limit, mine, ranked);
    return result;
  }

  private void saveLog(
      Long userId, int limit, List<UserSkill> mine, List<RecommendationEngine.Ranked> ranked) {
    var first = ranked.get(0);
    int matched =
        (int)
            first.skills().stream()
                .filter(
                    s ->
                        mine.stream()
                            .anyMatch(
                                u -> Objects.equals(u.getSkill().getId(), s.getSkill().getId())))
                .count();
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("limit", limit);
    snapshot.put("userSkillIds", mine.stream().map(s -> s.getSkill().getId()).toList());
    snapshot.put(
        "topRecommendations",
        ranked.stream()
            .map(
                r ->
                    Map.of(
                        "rank",
                        ranked.indexOf(r) + 1,
                        "occupationId",
                        r.occupation().getId(),
                        "finalScore",
                        RecommendationEngine.round1(r.score().finalScore())))
            .toList());
    try {
      logs.save(
          new RecommendationLog(
              users.getReferenceById(userId),
              first.occupation(),
              BigDecimal.valueOf(RecommendationEngine.clamp(first.score().finalScore()))
                  .setScale(2, RoundingMode.HALF_UP),
              first.skills().size() - matched,
              mapper.writeValueAsString(snapshot)));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Could not serialize recommendation audit snapshot", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public SkillGapResponse getSkillGap(Long userId, Long occupationId) {
    Occupation o =
        occupations
            .findById(occupationId)
            .orElseThrow(() -> new ResourceNotFoundException("Occupation", "id", occupationId));
    List<UserSkill> mine = userSkills.findByUserId(userId);
    Map<Long, UserSkill> mineById =
        mine.stream().collect(Collectors.toMap(s -> s.getSkill().getId(), s -> s, (a, b) -> a));
    List<OccupationSkill> required = occupationSkills.findByOccupationIdWithSkills(occupationId);
    List<MatchedSkillResponse> matched = new ArrayList<>();
    List<MissingSkillResponse> essential = new ArrayList<>(), optional = new ArrayList<>();
    for (var os : required) {
      var s = os.getSkill();
      var w = os.getImportanceWeight();
      String importance = RecommendationEngine.importance(w);
      var user = mineById.get(s.getId());
      if (user != null)
        matched.add(
            new MatchedSkillResponse(
                s.getId(), s.getName(), user.getProficiencyLevel(), importance, w));
      else {
        boolean req = Boolean.TRUE.equals(os.getIsRequired());
        var m =
            new MissingSkillResponse(
                s.getId(),
                s.getName(),
                importance,
                w,
                RecommendationEngine.priority(req, w),
                "Develop this skill to improve your match for this occupation.");
        (req ? essential : optional).add(m);
      }
    }
    Comparator<BigDecimal> weights = Comparator.nullsFirst(Comparator.reverseOrder());
    Comparator<MatchedSkillResponse> mc =
        Comparator.comparing(MatchedSkillResponse::importanceWeight, weights)
            .thenComparing(MatchedSkillResponse::skillId);
    Comparator<MissingSkillResponse> xc =
        Comparator.comparing(MissingSkillResponse::importanceWeight, weights)
            .thenComparing(MissingSkillResponse::skillId);
    matched.sort(mc);
    essential.sort(xc);
    optional.sort(xc);
    double pct = engine.skillMatch(mine, required);
    return new SkillGapResponse(
        o.getId(), o.getTitle(), RecommendationEngine.round1(pct), matched, essential, optional);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<RecommendationHistoryItemResponse> getRecommendationHistory(
      Long userId, int page, int size) {
    if (userId == null) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authenticated candidate is required.");
    }
    if (page < 0 || size < 1 || size > MAX_HISTORY_PAGE_SIZE) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "INVALID_PAGINATION",
          "Page must be >= 0 and size must be between 1 and " + MAX_HISTORY_PAGE_SIZE + ".");
    }
    Page<RecommendationLog> historyPage =
        logs.findHistoryByUserId(userId, PageRequest.of(page, size, HISTORY_SORT));
    return PageResponse.from(historyPage.map(this::toHistoryItem));
  }

  private RecommendationHistoryItemResponse toHistoryItem(RecommendationLog log) {
    Occupation occupation = log.getOccupation();
    Long occupationId = occupation == null ? null : occupation.getId();
    String occupationCode = occupation == null ? null : occupation.getCode();
    String occupationTitle = occupation == null ? null : occupation.getTitle();
    String groupName =
        occupation == null || occupation.getGroup() == null
            ? null
            : occupation.getGroup().getName();
    int skillGapCount = log.getSkillGapCount() == null ? 0 : log.getSkillGapCount();
    return new RecommendationHistoryItemResponse(
        log.getId(),
        occupationId,
        occupationCode,
        occupationTitle,
        groupName,
        log.getMatchScore(),
        skillGapCount,
        toInstant(log.getRecommendationDate()),
        toInstant(log.getCreatedAt()),
        parseSnapshot(log.getInputSnapshotJson()));
  }

  private static Instant toInstant(OffsetDateTime dateTime) {
    return dateTime == null ? null : dateTime.toInstant();
  }

  private JsonNode parseSnapshot(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      return null;
    }
    try {
      return mapper.readTree(rawJson);
    } catch (JsonProcessingException ex) {
      LOG.warn("Could not parse stored recommendation snapshot JSON");
      return null;
    }
  }

  private void validateLimit(int limit) {
    if (limit < 1 || limit > 50)
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "INVALID_LIMIT", "Limit must be between 1 and 50");
  }
}

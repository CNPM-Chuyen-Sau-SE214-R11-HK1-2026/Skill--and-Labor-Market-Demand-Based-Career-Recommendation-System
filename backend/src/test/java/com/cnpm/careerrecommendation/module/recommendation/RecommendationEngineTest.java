package com.cnpm.careerrecommendation.module.recommendation;

import static org.junit.jupiter.api.Assertions.*;

import com.cnpm.careerrecommendation.common.entity.Skill;
import com.cnpm.careerrecommendation.entity.*;
import com.cnpm.careerrecommendation.module.recommendation.service.RecommendationEngine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationEngineTest {
  private final RecommendationEngine engine = new RecommendationEngine();

  @Test
  @DisplayName("SkillMatch applies proficiency multipliers and weighted average")
  void weightedSkillMatch() {
    Occupation o = new Occupation("X", "X", null, null);
    Skill a = skill(1L, "A"), b = skill(2L, "B"), c = skill(3L, "C"), d = skill(4L, "D");
    var req =
        List.of(
            new OccupationSkill(o, a, BigDecimal.ONE, true),
            new OccupationSkill(o, b, BigDecimal.ONE, true),
            new OccupationSkill(o, c, BigDecimal.ONE, true),
            new OccupationSkill(o, d, BigDecimal.ONE, true));
    var users =
        List.of(
            user(a, "BEGINNER"), user(b, "INTERMEDIATE"), user(c, "ADVANCED"), user(d, "EXPERT"));
    assertEquals(82.5, engine.skillMatch(users, req), 0.001);
  }

  @Test
  @DisplayName("Empty user, occupation skills, and zero weights produce zero skill match")
  void emptyAndZeroWeights() {
    Skill s = skill(1L, "A");
    Occupation o = new Occupation("X", "X", null, null);
    assertEquals(0, engine.skillMatch(List.of(user(s, "EXPERT")), List.of()));
    assertEquals(
        0, engine.skillMatch(List.of(), List.of(new OccupationSkill(o, s, BigDecimal.ONE, true))));
    assertEquals(
        0,
        engine.skillMatch(
            List.of(user(s, "EXPERT")), List.of(new OccupationSkill(o, s, BigDecimal.ZERO, true))));
  }

  @Test
  @DisplayName("SkillMatch is zero without overlap and one hundred for advanced full coverage")
  void bounds() {
    Skill a = skill(1L, "A"), b = skill(2L, "B");
    Occupation o = new Occupation("X", "X", null, null);
    var req = List.of(new OccupationSkill(o, a, BigDecimal.ONE, true));
    assertEquals(0, engine.skillMatch(List.of(user(b, "EXPERT")), req));
    assertEquals(100, engine.skillMatch(List.of(user(a, "ADVANCED")), req));
  }

  @Test
  @DisplayName("Preferences are neutral when absent and compare active industry criterion")
  void preference() {
    Occupation o = new Occupation("X", "X", "Technology", null);
    CandidateProfile p = new CandidateProfile();
    assertEquals(50, engine.preferenceMatch(o, p));
    p.setTargetIndustry("Technology");
    assertEquals(100, engine.preferenceMatch(o, p));
    p.setTargetIndustry("Healthcare");
    assertEquals(0, engine.preferenceMatch(o, p));
  }

  @Test
  @DisplayName("Missing demand indicators use neutral demand")
  void missingDemand() {
    var score =
        engine.score(new Occupation("X", "X", null, null), List.of(), List.of(), null, List.of());
    assertEquals(50, score.demandScore());
    assertNull(score.indicator());
  }

  @Test
  @DisplayName("Salary boundary within twenty percent is partial fit")
  void salaryTolerance() {
    Occupation o = new Occupation("X", "X", null, null);
    o.setEntrySalaryAvg(new BigDecimal("100"));
    o.setMidSalaryAvg(new BigDecimal("100"));
    CandidateProfile p = new CandidateProfile();
    p.setExpectedSalaryMin(new BigDecimal("120"));
    assertEquals(50, engine.preferenceMatch(o, p));
  }

  @Test
  @DisplayName("Demand selection falls back from location to national to latest")
  void demandFallback() {
    Occupation o = new Occupation("X", "X", null, null);
    var local = mdi(o, "HANOI", 80);
    var national = mdi(o, "NATIONAL", 60);
    var other = mdi(o, "OTHER", 30);
    CandidateProfile p = new CandidateProfile();
    p.setPreferredLocation("hanoi");
    assertEquals("HANOI", engine.selectIndicator(p, List.of(other, national, local)).getRegion());
    p.setPreferredLocation("missing");
    assertEquals("NATIONAL", engine.selectIndicator(p, List.of(other, national)).getRegion());
    assertEquals("NATIONAL", engine.selectIndicator(null, List.of(other, national)).getRegion());
    assertEquals("OTHER", engine.selectIndicator(null, List.of(other)).getRegion());
  }

  @Test
  @DisplayName("Ranking applies final score, skill match, demand, then id")
  void rankingTies() {
    double[][] vals = {{80, 90, 50, 70}, {90, 70, 50, 70}, {90, 80, 60, 70}, {90, 80, 60, 70}};
    List<RecommendationEngine.Ranked> rows = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Occupation o = new Occupation("O" + i, "O", null, null);
      o.setId(4L - i);
      rows.add(
          new RecommendationEngine.Ranked(
              o,
              new RecommendationEngine.Score(vals[i][0], vals[i][1], vals[i][2], vals[i][3], null),
              List.of()));
    }
    rows.sort(RecommendationEngine.rankingOrder());
    assertEquals(List.of(1L, 2L, 3L, 4L), rows.stream().map(r -> r.occupation().getId()).toList());
  }

  @Test
  @DisplayName("NFR-02 ranks one thousand occupations within 500ms")
  void benchmark() {
    List<Skill> skills = new ArrayList<>();
    for (long i = 1; i <= 10; i++) skills.add(skill(i, "S" + i));
    List<UserSkill> mine = skills.stream().map(s -> user(s, "ADVANCED")).toList();
    List<RecommendationEngine.Ranked> ranked = new ArrayList<>();
    long start = System.nanoTime();
    for (long i = 1; i <= 1000; i++) {
      Occupation o = new Occupation("O" + i, "Occupation" + i, null, null);
      o.setId(i);
      List<OccupationSkill> req =
          skills.stream().map(s -> new OccupationSkill(o, s, BigDecimal.ONE, true)).toList();
      var indicator = mdi(o, "NATIONAL", 75);
      ranked.add(
          new RecommendationEngine.Ranked(
              o, engine.score(o, mine, req, null, List.of(indicator)), req));
    }
    ranked.sort(RecommendationEngine.rankingOrder());
    List<?> top = ranked.stream().limit(10).toList();
    long elapsed = (System.nanoTime() - start) / 1_000_000;
    System.out.println(
        "NFR-02 benchmark: 1000 occupations x 10 skills, 1000 indicators, Top-10: "
            + elapsed
            + " ms");
    assertEquals(10, top.size());
    assertTrue(elapsed < 500, "benchmark took " + elapsed + " ms");
  }

  private static MarketDemandIndicator mdi(Occupation o, String region, int demand) {
    return new MarketDemandIndicator(o, region, new BigDecimal(demand), java.time.LocalDate.now());
  }

  private static Skill skill(Long id, String name) {
    Skill s = new Skill(name, name, "TECHNICAL", null);
    s.setId(id);
    return s;
  }

  private static UserSkill user(Skill skill, String level) {
    UserSkill s = new UserSkill();
    s.setSkill(skill);
    s.setProficiencyLevel(level);
    return s;
  }
}

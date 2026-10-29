package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.OccupationSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link OccupationSkill} entity.
 */
@Repository
public interface OccupationSkillRepository extends JpaRepository<OccupationSkill, Long> {

    List<OccupationSkill> findByOccupationId(Long occupationId);

    Optional<OccupationSkill> findByOccupationIdAndSkillId(Long occupationId, Long skillId);

    boolean existsByOccupationIdAndSkillId(Long occupationId, Long skillId);

    void deleteByOccupationIdAndSkillId(Long occupationId, Long skillId);
}


package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.OccupationSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
        SELECT os FROM OccupationSkill os
        JOIN FETCH os.skill s
        LEFT JOIN FETCH s.category
        WHERE os.occupation.id = :occupationId
        ORDER BY os.importanceWeight DESC
    """)
    List<OccupationSkill> findByOccupationIdWithSkills(@Param("occupationId") Long occupationId);
}

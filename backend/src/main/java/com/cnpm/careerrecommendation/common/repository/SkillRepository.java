package com.cnpm.careerrecommendation.common.repository;

import com.cnpm.careerrecommendation.common.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link Skill} entity.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByCode(String code);

    boolean existsByCode(String code);

    List<Skill> findByCategoryId(Long categoryId);

    List<Skill> findByNameContainingIgnoreCase(String name);

    List<Skill> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name);
}

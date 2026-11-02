package com.cnpm.careerrecommendation.common.repository;

import com.cnpm.careerrecommendation.common.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link SkillCategory} entity.
 */
@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {

    Optional<SkillCategory> findByCode(String code);
}

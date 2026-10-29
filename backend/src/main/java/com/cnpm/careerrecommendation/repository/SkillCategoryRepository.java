package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link SkillCategory} entity.
 */
@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {

    Optional<SkillCategory> findByCode(String code);

    boolean existsByCode(String code);
}


package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.UserSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link UserSkill} entity.
 */
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    @EntityGraph(attributePaths = {"skill", "skill.category"})
    List<UserSkill> findByUserId(Long userId);
    long countByUserId(Long userId);

    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);

    boolean existsByUserIdAndSkillId(Long userId, Long skillId);

    void deleteByUserIdAndSkillId(Long userId, Long skillId);
}


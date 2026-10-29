package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link Occupation} entity.
 */
@Repository
public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    Optional<Occupation> findByCode(String code);

    boolean existsByCode(String code);

    List<Occupation> findByGroupId(Long groupId);

    List<Occupation> findByIndustry(String industry);

    List<Occupation> findByTitleContainingIgnoreCase(String keyword);
}


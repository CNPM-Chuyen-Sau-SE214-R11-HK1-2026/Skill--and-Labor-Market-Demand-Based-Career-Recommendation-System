package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.OccupationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link OccupationGroup} entity.
 */
@Repository
public interface OccupationGroupRepository extends JpaRepository<OccupationGroup, Long> {

    Optional<OccupationGroup> findByCode(String code);

    boolean existsByCode(String code);
}


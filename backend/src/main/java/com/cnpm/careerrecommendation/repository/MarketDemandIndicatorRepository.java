package com.cnpm.careerrecommendation.repository;

import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link MarketDemandIndicator} entity.
 */
@Repository
public interface MarketDemandIndicatorRepository extends JpaRepository<MarketDemandIndicator, Long> {

    List<MarketDemandIndicator> findByOccupationId(Long occupationId);

    Optional<MarketDemandIndicator> findByOccupationIdAndRegionAndSnapshotDate(Long occupationId, String region, LocalDate snapshotDate);

    List<MarketDemandIndicator> findByOccupationIdAndRegionOrderBySnapshotDateDesc(Long occupationId, String region);
}


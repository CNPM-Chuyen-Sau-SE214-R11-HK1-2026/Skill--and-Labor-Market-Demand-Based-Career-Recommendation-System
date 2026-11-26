package com.cnpm.careerrecommendation.module.lmi;

import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.entity.OccupationGroup;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiBatchItemDto;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportRequest;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportResponse;
import com.cnpm.careerrecommendation.module.lmi.service.LmiIngestionService;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LmiServiceTest {

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private MarketDemandIndicatorRepository marketDemandIndicatorRepository;

    private LmiIngestionService lmiIngestionService;

    private Occupation softwareDev;
    private Occupation dataEngineer;

    @BeforeEach
    void setUp() {
        lmiIngestionService = new LmiIngestionService(occupationRepository, marketDemandIndicatorRepository);

        OccupationGroup group = new OccupationGroup("GRP-SWE", "Software Engineering", "Desc");
        group.setId(1L);

        softwareDev = new Occupation("SOC-15-1252", "Software Developer", "IT", group);
        softwareDev.setId(101L);

        dataEngineer = new Occupation("SOC-15-2052", "Data Engineer", "IT", group);
        dataEngineer.setId(102L);
    }

    @Test
    @DisplayName("importMarketData: inserts new records by ID and code with normalized defaults")
    void importMarketData_InsertsNewIndicatorsSuccessfully() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        LmiBatchItemDto byId = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), null, " hcmc ", new BigDecimal("88.50"),
                new BigDecimal("12.40"), BigDecimal.valueOf(1250), new BigDecimal("2.80"), snapshot);
        LmiBatchItemDto byCode = new LmiBatchItemDto(
                null, " SOC-15-2052 ", null, new BigDecimal("90.00"),
                new BigDecimal("-5.25"), null, null, snapshot);

        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(occupationRepository.findByCodeIn(anyCollection())).thenReturn(List.of(dataEngineer));
        when(marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        LmiImportResponse response = lmiIngestionService.importMarketData(new LmiImportRequest(List.of(byId, byCode)));

        assertThat(response.totalRecordsProcessed()).isEqualTo(2);
        assertThat(response.recordsInserted()).isEqualTo(2);
        assertThat(response.recordsUpdated()).isZero();
        assertThat(response.timestamp()).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<MarketDemandIndicator>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(marketDemandIndicatorRepository).saveAllAndFlush(captor.capture());

        List<MarketDemandIndicator> saved = List.copyOf(captor.getValue());
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getOccupation().getId()).isEqualTo(101L);
        assertThat(saved.get(0).getRegion()).isEqualTo("HCMC");
        assertThat(saved.get(0).getDemandScore()).isEqualByComparingTo("88.50");
        assertThat(saved.get(0).getOpenJobPostingsCount()).isEqualTo(1250);

        assertThat(saved.get(1).getOccupation().getId()).isEqualTo(102L);
        assertThat(saved.get(1).getRegion()).isEqualTo("NATIONAL");
        assertThat(saved.get(1).getGrowthRatePct()).isEqualByComparingTo("-5.25");
        assertThat(saved.get(1).getOpenJobPostingsCount()).isZero();
    }

    @Test
    @DisplayName("importMarketData: upserts existing indicator matching (occupationId, region, snapshotDate)")
    void importMarketData_UpsertsExistingIndicatorSuccessfully() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        MarketDemandIndicator existing = new MarketDemandIndicator(
                softwareDev, "hanoi", new BigDecimal("75.00"), snapshot);
        existing.setId(500L);
        existing.setOpenJobPostingsCount(300);

        LmiBatchItemDto updateItem = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), "SOC-15-1252", "hanoi", new BigDecimal("92.25"),
                new BigDecimal("15.00"), BigDecimal.valueOf(640), new BigDecimal("3.10"), snapshot);

        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(occupationRepository.findByCodeIn(anyCollection())).thenReturn(List.of(softwareDev));
        when(marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of(existing));

        LmiImportResponse response = lmiIngestionService.importMarketData(new LmiImportRequest(List.of(updateItem)));

        assertThat(response.totalRecordsProcessed()).isEqualTo(1);
        assertThat(response.recordsInserted()).isZero();
        assertThat(response.recordsUpdated()).isEqualTo(1);
        assertThat(existing.getRegion()).isEqualTo("HANOI");
        assertThat(existing.getDemandScore()).isEqualByComparingTo("92.25");
        assertThat(existing.getGrowthRatePct()).isEqualByComparingTo("15.00");
        assertThat(existing.getOpenJobPostingsCount()).isEqualTo(640);
        assertThat(existing.getCompetitiveIndex()).isEqualByComparingTo("3.10");
    }

    @Test
    @DisplayName("importMarketData: merges duplicate natural keys inside the same batch")
    void importMarketData_DeduplicatesSameNaturalKeyWithinBatch() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        LmiBatchItemDto first = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"),
                new BigDecimal("10.00"), BigDecimal.valueOf(100), null, snapshot);
        LmiBatchItemDto duplicateInBatch = new LmiBatchItemDto(
                null, "SOC-15-1252", "national", new BigDecimal("85.00"),
                new BigDecimal("12.00"), BigDecimal.valueOf(250), new BigDecimal("2.50"), snapshot);

        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(occupationRepository.findByCodeIn(anyCollection())).thenReturn(List.of(softwareDev));
        when(marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        LmiImportResponse response = lmiIngestionService.importMarketData(
                new LmiImportRequest(List.of(first, duplicateInBatch)));

        assertThat(response.totalRecordsProcessed()).isEqualTo(2);
        assertThat(response.recordsInserted()).isEqualTo(1);
        assertThat(response.recordsUpdated()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<MarketDemandIndicator>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(marketDemandIndicatorRepository).saveAllAndFlush(captor.capture());

        List<MarketDemandIndicator> saved = List.copyOf(captor.getValue());
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getDemandScore()).isEqualByComparingTo("85.00");
        assertThat(saved.get(0).getOpenJobPostingsCount()).isEqualTo(250);
    }

    @Test
    @DisplayName("importMarketData: rejects null, empty, or oversized (>500) batch requests")
    void importMarketData_RejectsEmptyOrOversizedRequest() {
        assertThatThrownBy(() -> lmiIngestionService.importMarketData(null))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(api.getErrorCode()).isEqualTo("INVALID_LMI_REQUEST");
                });

        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of())))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        LmiBatchItemDto sample = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"),
                null, BigDecimal.TEN, null, LocalDate.of(2026, 11, 1));
        List<LmiBatchItemDto> oversized = Collections.nCopies(LmiImportRequest.MAX_BATCH_SIZE + 1, sample);
        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(oversized)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(api.getErrorCode()).isEqualTo("INVALID_LMI_REQUEST");
                });

        verify(marketDemandIndicatorRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("importMarketData: rejects invalid decimals, fractional integers, negative postings, and missing identifiers")
    void importMarketData_RejectsInvalidFieldValues() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);

        List<LmiBatchItemDto> invalidItems = List.of(
                new LmiBatchItemDto(null, "  ", "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.ZERO, null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(new BigDecimal("101.5"), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("-0.01"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("100.01"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("88.123"), null, BigDecimal.TEN, null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.valueOf(-1), null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"), null, new BigDecimal("10.9"), null, snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, new BigDecimal("10.00"), snapshot),
                new LmiBatchItemDto(BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, null)
        );

        for (LmiBatchItemDto invalid : invalidItems) {
            assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of(invalid))))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(api.getErrorCode()).isEqualTo("INVALID_LMI_REQUEST");
                    });
        }

        verify(marketDemandIndicatorRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("importMarketData: rejects non-existent occupationId, unknown code, or mismatched id/code")
    void importMarketData_RejectsUnknownOrMismatchedOccupation() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);

        LmiBatchItemDto unknownId = new LmiBatchItemDto(
                BigDecimal.valueOf(999L), null, "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot);
        when(occupationRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of(unknownId))))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(api.getErrorCode()).isEqualTo("INVALID_OCCUPATION");
                });

        LmiBatchItemDto mismatched = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), "SOC-15-2052", "NATIONAL", new BigDecimal("80.00"), null, BigDecimal.TEN, null, snapshot);
        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(occupationRepository.findByCodeIn(anyCollection())).thenReturn(List.of(dataEngineer));

        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of(mismatched))))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(api.getErrorCode()).isEqualTo("INVALID_OCCUPATION");
                });

        verify(marketDemandIndicatorRepository, never()).saveAllAndFlush(any());
    }

    @Test
    @DisplayName("importMarketData: maps uq_market_demand_occ_region_date constraint violation to 409 CONFLICT")
    void importMarketData_MapsUniqueConstraintViolationToConflict() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        LmiBatchItemDto item = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("88.50"), null, BigDecimal.valueOf(100), null, snapshot);

        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        ConstraintViolationException duplicateKey = new ConstraintViolationException(
                "duplicate key", new SQLException("duplicate"), "uq_market_demand_occ_region_date");
        when(marketDemandIndicatorRepository.saveAllAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("constraint", duplicateKey));

        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of(item))))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(api.getErrorCode()).isEqualTo("LMI_CONCURRENT_CONFLICT");
                });
    }

    @Test
    @DisplayName("importMarketData: rethrows unrelated DataIntegrityViolationException without masking")
    void importMarketData_RethrowsUnrelatedDataIntegrityViolation() {
        LocalDate snapshot = LocalDate.of(2026, 11, 1);
        LmiBatchItemDto item = new LmiBatchItemDto(
                BigDecimal.valueOf(101L), null, "NATIONAL", new BigDecimal("88.50"), null, BigDecimal.valueOf(100), null, snapshot);

        when(occupationRepository.findAllById(any())).thenReturn(List.of(softwareDev));
        when(marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(anyCollection(), anyCollection()))
                .thenReturn(List.of());

        ConstraintViolationException otherConstraint = new ConstraintViolationException(
                "fk error", new SQLException("fk"), "fk_other_constraint");
        when(marketDemandIndicatorRepository.saveAllAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("other", otherConstraint));

        assertThatThrownBy(() -> lmiIngestionService.importMarketData(new LmiImportRequest(List.of(item))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

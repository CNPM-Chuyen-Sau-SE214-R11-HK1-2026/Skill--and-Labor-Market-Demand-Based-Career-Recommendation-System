package com.cnpm.careerrecommendation.module.lmi.service;

import com.cnpm.careerrecommendation.common.exception.ApiException;
import com.cnpm.careerrecommendation.entity.MarketDemandIndicator;
import com.cnpm.careerrecommendation.entity.Occupation;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiBatchItemDto;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportRequest;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportResponse;
import com.cnpm.careerrecommendation.repository.MarketDemandIndicatorRepository;
import com.cnpm.careerrecommendation.repository.OccupationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling validated batch ingestion (upsert) of Labor Market Information (LMI) indicators.
 */
@Service
public class LmiIngestionService {

    private static final BigDecimal MIN_DEMAND_SCORE = BigDecimal.ZERO;
    private static final BigDecimal MAX_DEMAND_SCORE = new BigDecimal("100.00");
    private static final BigDecimal MIN_GROWTH_RATE = new BigDecimal("-999.99");
    private static final BigDecimal MAX_GROWTH_RATE = new BigDecimal("999.99");
    private static final BigDecimal MIN_COMPETITIVE_INDEX = BigDecimal.ZERO;
    private static final BigDecimal MAX_COMPETITIVE_INDEX = new BigDecimal("9.99");
    private static final int MAX_DECIMAL_SCALE = 2;
    private static final int MAX_CODE_OR_REGION_LENGTH = 50;
    private static final String LMI_UNIQUE_CONSTRAINT = "uq_market_demand_occ_region_date";

    private final OccupationRepository occupationRepository;
    private final MarketDemandIndicatorRepository marketDemandIndicatorRepository;

    public LmiIngestionService(OccupationRepository occupationRepository,
                               MarketDemandIndicatorRepository marketDemandIndicatorRepository) {
        this.occupationRepository = occupationRepository;
        this.marketDemandIndicatorRepository = marketDemandIndicatorRepository;
    }

    /**
     * Validates and upserts a batch of LMI demand indicators atomically.
     *
     * @param request batch import request containing between 1 and {@link LmiImportRequest#MAX_BATCH_SIZE} items
     * @return summary counts of processed, inserted, and updated records
     */
    @Transactional
    public LmiImportResponse importMarketData(LmiImportRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw invalidRequest("Danh sách chỉ số LMI không được rỗng.");
        }
        if (request.items().size() > LmiImportRequest.MAX_BATCH_SIZE) {
            throw invalidRequest("Mỗi lần nạp tối đa " + LmiImportRequest.MAX_BATCH_SIZE + " bản ghi LMI.");
        }

        List<NormalizedItem> normalizedItems = new ArrayList<>(request.items().size());
        for (int i = 0; i < request.items().size(); i++) {
            normalizedItems.add(validateAndNormalize(request.items().get(i), i));
        }

        List<ResolvedItem> resolvedItems = resolveOccupations(normalizedItems);
        Map<LmiNaturalKey, MarketDemandIndicator> existingByKey = fetchExistingByNaturalKey(resolvedItems);
        Map<LmiNaturalKey, MarketDemandIndicator> stagedForSave = new LinkedHashMap<>();

        int inserted = 0;
        int updated = 0;

        for (ResolvedItem item : resolvedItems) {
            LmiNaturalKey key = new LmiNaturalKey(
                    item.occupation().getId(),
                    item.normalized().region(),
                    item.normalized().snapshotDate()
            );

            MarketDemandIndicator indicator = existingByKey.get(key);
            if (indicator == null) {
                indicator = new MarketDemandIndicator(
                        item.occupation(),
                        item.normalized().region(),
                        item.normalized().demandScore(),
                        item.normalized().snapshotDate()
                );
                applyMetrics(indicator, item.normalized());
                existingByKey.put(key, indicator);
                inserted++;
            } else {
                applyMetrics(indicator, item.normalized());
                updated++;
            }
            stagedForSave.put(key, indicator);
        }

        try {
            marketDemandIndicatorRepository.saveAllAndFlush(stagedForSave.values());
        } catch (DataIntegrityViolationException ex) {
            if (isLmiUniqueConstraintViolation(ex)) {
                throw new ApiException(HttpStatus.CONFLICT, "LMI_CONCURRENT_CONFLICT",
                        "Xung đột dữ liệu khi nạp đồng thời chỉ số LMI. Vui lòng thử lại.");
            }
            throw ex;
        }

        return new LmiImportResponse(
                resolvedItems.size(),
                inserted,
                updated,
                Instant.now()
        );
    }

    private NormalizedItem validateAndNormalize(LmiBatchItemDto raw, int index) {
        if (raw == null) {
            throw invalidRequest("Phần tử tại vị trí " + index + " không được null.");
        }

        Long occupationId = parseOptionalPositiveLong(raw.occupationId(), "occupationId", index);

        String occupationCode = normalizeOptionalText(raw.occupationCode());
        if (occupationCode != null && occupationCode.length() > MAX_CODE_OR_REGION_LENGTH) {
            throw invalidRequest("occupationCode tối đa 50 ký tự (vị trí " + index + ").");
        }
        if (occupationId == null && occupationCode == null) {
            throw invalidRequest("Phải cung cấp occupationId hoặc occupationCode (vị trí " + index + ").");
        }

        String regionRaw = normalizeOptionalText(raw.region());
        String region = (regionRaw == null)
                ? MarketDemandIndicator.REGION_NATIONAL
                : regionRaw.toUpperCase(Locale.ROOT);
        if (region.length() > MAX_CODE_OR_REGION_LENGTH) {
            throw invalidRequest("region tối đa 50 ký tự (vị trí " + index + ").");
        }

        BigDecimal demandScore = raw.demandScore();
        if (demandScore == null) {
            throw invalidRequest("demandScore không được để trống (vị trí " + index + ").");
        }
        validateDecimalRangeAndScale("demandScore", demandScore, MIN_DEMAND_SCORE, MAX_DEMAND_SCORE, index);

        BigDecimal growthRatePct = raw.growthRatePct();
        if (growthRatePct != null) {
            validateDecimalRangeAndScale("growthRatePct", growthRatePct, MIN_GROWTH_RATE, MAX_GROWTH_RATE, index);
        }

        Integer openJobPostingsCount = parseOptionalNonNegativeInt(raw.openJobPostingsCount(), index);

        BigDecimal competitiveIndex = raw.competitiveIndex();
        if (competitiveIndex != null) {
            validateDecimalRangeAndScale("competitiveIndex", competitiveIndex, MIN_COMPETITIVE_INDEX, MAX_COMPETITIVE_INDEX, index);
        }

        LocalDate snapshotDate = raw.snapshotDate();
        if (snapshotDate == null) {
            throw invalidRequest("snapshotDate không được để trống (vị trí " + index + ").");
        }

        return new NormalizedItem(
                occupationId,
                occupationCode,
                region,
                demandScore,
                growthRatePct,
                openJobPostingsCount,
                competitiveIndex,
                snapshotDate
        );
    }

    private Long parseOptionalPositiveLong(BigDecimal value, String fieldName, int index) {
        if (value == null) {
            return null;
        }
        if (value.signum() <= 0 || value.scale() > 0) {
            throw invalidRequest(fieldName + " phải là số nguyên dương (vị trí " + index + ").");
        }
        try {
            return value.longValueExact();
        } catch (ArithmeticException ex) {
            throw invalidRequest(fieldName + " phải là số nguyên dương hợp lệ (vị trí " + index + ").");
        }
    }

    private Integer parseOptionalNonNegativeInt(BigDecimal value, int index) {
        if (value == null) {
            return 0;
        }
        if (value.signum() < 0 || value.scale() > 0) {
            throw invalidRequest("openJobPostingsCount phải là số nguyên không âm (vị trí " + index + ").");
        }
        try {
            return value.intValueExact();
        } catch (ArithmeticException ex) {
            throw invalidRequest("openJobPostingsCount vượt quá giới hạn cho phép (vị trí " + index + ").");
        }
    }

    private void validateDecimalRangeAndScale(String fieldName, BigDecimal value,
                                              BigDecimal min, BigDecimal max, int index) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw invalidRequest(fieldName + " phải nằm trong khoảng " + min + " đến " + max + " (vị trí " + index + ").");
        }
        if (value.stripTrailingZeros().scale() > MAX_DECIMAL_SCALE) {
            throw invalidRequest(fieldName + " tối đa 2 chữ số thập phân (vị trí " + index + ").");
        }
    }

    private List<ResolvedItem> resolveOccupations(List<NormalizedItem> items) {
        Set<Long> requestedIds = new HashSet<>();
        Set<String> requestedCodes = new HashSet<>();
        for (NormalizedItem item : items) {
            if (item.occupationId() != null) {
                requestedIds.add(item.occupationId());
            }
            if (item.occupationCode() != null) {
                requestedCodes.add(item.occupationCode());
            }
        }

        Map<Long, Occupation> byId = requestedIds.isEmpty()
                ? Map.of()
                : occupationRepository.findAllById(requestedIds).stream()
                        .collect(Collectors.toMap(Occupation::getId, Function.identity()));

        Map<String, Occupation> byCode = requestedCodes.isEmpty()
                ? Map.of()
                : occupationRepository.findByCodeIn(requestedCodes).stream()
                        .collect(Collectors.toMap(Occupation::getCode, Function.identity(), (a, b) -> a));

        List<ResolvedItem> resolved = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            NormalizedItem item = items.get(i);
            Occupation occById = (item.occupationId() != null) ? byId.get(item.occupationId()) : null;
            if (item.occupationId() != null && occById == null) {
                throw invalidOccupation("Không tìm thấy nghề nghiệp với occupationId=" + item.occupationId() + " (vị trí " + i + ").");
            }

            Occupation occByCode = (item.occupationCode() != null) ? byCode.get(item.occupationCode()) : null;
            if (item.occupationCode() != null && occByCode == null) {
                throw invalidOccupation("Không tìm thấy nghề nghiệp với occupationCode=" + item.occupationCode() + " (vị trí " + i + ").");
            }

            if (occById != null && occByCode != null && !Objects.equals(occById.getId(), occByCode.getId())) {
                throw invalidOccupation("occupationId và occupationCode không trỏ tới cùng một nghề nghiệp (vị trí " + i + ").");
            }

            Occupation target = (occById != null) ? occById : occByCode;
            resolved.add(new ResolvedItem(target, item));
        }
        return resolved;
    }

    private Map<LmiNaturalKey, MarketDemandIndicator> fetchExistingByNaturalKey(List<ResolvedItem> items) {
        Set<Long> occIds = new HashSet<>();
        Set<LocalDate> dates = new HashSet<>();
        for (ResolvedItem item : items) {
            if (item.occupation().getId() != null) {
                occIds.add(item.occupation().getId());
            }
            dates.add(item.normalized().snapshotDate());
        }

        Map<LmiNaturalKey, MarketDemandIndicator> map = new HashMap<>();
        if (occIds.isEmpty() || dates.isEmpty()) {
            return map;
        }

        List<MarketDemandIndicator> existingList =
                marketDemandIndicatorRepository.findByOccupationIdInAndSnapshotDateIn(occIds, dates);
        for (MarketDemandIndicator indicator : existingList) {
            String normalizedRegion = indicator.getRegion() == null
                    ? MarketDemandIndicator.REGION_NATIONAL
                    : indicator.getRegion().trim().toUpperCase(Locale.ROOT);
            LmiNaturalKey key = new LmiNaturalKey(
                    indicator.getOccupation().getId(),
                    normalizedRegion,
                    indicator.getSnapshotDate()
            );
            map.put(key, indicator);
        }
        return map;
    }

    private void applyMetrics(MarketDemandIndicator indicator, NormalizedItem item) {
        indicator.setRegion(item.region());
        indicator.setDemandScore(item.demandScore());
        indicator.setGrowthRatePct(item.growthRatePct());
        indicator.setOpenJobPostingsCount(item.openJobPostingsCount());
        indicator.setCompetitiveIndex(item.competitiveIndex());
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isLmiUniqueConstraintViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && LMI_UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    private ApiException invalidRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LMI_REQUEST", message);
    }

    private ApiException invalidOccupation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OCCUPATION", message);
    }

    private record NormalizedItem(
            Long occupationId,
            String occupationCode,
            String region,
            BigDecimal demandScore,
            BigDecimal growthRatePct,
            Integer openJobPostingsCount,
            BigDecimal competitiveIndex,
            LocalDate snapshotDate
    ) {
    }

    private record ResolvedItem(Occupation occupation, NormalizedItem normalized) {
    }

    private record LmiNaturalKey(Long occupationId, String region, LocalDate snapshotDate) {
    }
}

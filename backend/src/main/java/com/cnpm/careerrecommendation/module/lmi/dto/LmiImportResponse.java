package com.cnpm.careerrecommendation.module.lmi.dto;

import java.time.Instant;

/**
 * Summary response returned after completing a batch LMI import operation.
 */
public record LmiImportResponse(
        int totalRecordsProcessed,
        int recordsInserted,
        int recordsUpdated,
        Instant timestamp
) {
}

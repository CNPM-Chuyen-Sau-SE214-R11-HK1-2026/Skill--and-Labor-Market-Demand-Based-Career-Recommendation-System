package com.cnpm.careerrecommendation.module.lmi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Request payload for batch ingestion of labor market demand indicators.
 */
public record LmiImportRequest(
        @NotEmpty(message = "Danh sách chỉ số LMI không được rỗng")
        @Size(max = MAX_BATCH_SIZE, message = "Mỗi lần nạp tối đa 500 bản ghi LMI")
        List<@NotNull(message = "Phần tử chỉ số LMI không được null") @Valid LmiBatchItemDto> items
) {
    public static final int MAX_BATCH_SIZE = 500;
}

package com.cnpm.careerrecommendation.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public static <T> PageResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        boolean isLast = (pageNumber + 1) >= totalPages;
        return new PageResponse<>(content, pageNumber, pageSize, totalElements, totalPages, isLast);
    }

    public int page() {
        return pageNumber;
    }

    public int size() {
        return pageSize;
    }

    public boolean isLast() {
        return last;
    }
}

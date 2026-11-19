package com.cnpm.careerrecommendation.module.lmi.controller;

import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportRequest;
import com.cnpm.careerrecommendation.module.lmi.dto.LmiImportResponse;
import com.cnpm.careerrecommendation.module.lmi.service.LmiIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative REST controller for batch ingesting Labor Market Information (LMI) indicators.
 * Contract: docs/api/lmi.md
 */
@RestController
@RequestMapping("/api/v1/admin/lmi")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLmiController {

    private final LmiIngestionService lmiIngestionService;

    public AdminLmiController(LmiIngestionService lmiIngestionService) {
        this.lmiIngestionService = lmiIngestionService;
    }

    /**
     * Batch import (upsert) labor market demand indicators for one or more occupations.
     * Restricted to {@code ROLE_ADMIN}.
     */
    @PostMapping("/import")
    public ResponseEntity<LmiImportResponse> importMarketData(@Valid @RequestBody LmiImportRequest request) {
        LmiImportResponse response = lmiIngestionService.importMarketData(request);
        return ResponseEntity.ok(response);
    }
}

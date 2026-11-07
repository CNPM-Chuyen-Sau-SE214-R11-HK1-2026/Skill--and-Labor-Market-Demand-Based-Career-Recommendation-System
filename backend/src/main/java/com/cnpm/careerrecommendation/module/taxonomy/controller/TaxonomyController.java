package com.cnpm.careerrecommendation.module.taxonomy.controller;

import com.cnpm.careerrecommendation.common.dto.PageResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationDetailResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.OccupationSummaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.dto.SkillDictionaryResponse;
import com.cnpm.careerrecommendation.module.taxonomy.service.TaxonomyService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public REST controller for browsing occupations and standard skills dictionary.
 * Contract: docs/api/taxonomy.md
 */
@RestController
@RequestMapping("/api/v1")
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    /**
     * Search and paginate occupations by keyword and optional group filter.
     */
    @GetMapping("/occupations")
    public ResponseEntity<PageResponse<OccupationSummaryResponse>> searchOccupations(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "groupId", required = false) Long groupId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "title,asc") String sort
    ) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        PageResponse<OccupationSummaryResponse> response = taxonomyService.searchOccupations(query, groupId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get details of a single occupation including skills and market indicators.
     */
    @GetMapping("/occupations/{id}")
    public ResponseEntity<OccupationDetailResponse> getOccupationDetail(@PathVariable("id") Long id) {
        OccupationDetailResponse response = taxonomyService.getOccupationDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Search skills dictionary by query and optional category filter.
     */
    @GetMapping("/skills")
    public ResponseEntity<List<SkillDictionaryResponse>> searchSkills(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        List<SkillDictionaryResponse> response = taxonomyService.searchSkills(query, categoryId);
        return ResponseEntity.ok(response);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by("title").ascending();
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
            return Sort.by(property).descending();
        }
        return Sort.by(property).ascending();
    }
}

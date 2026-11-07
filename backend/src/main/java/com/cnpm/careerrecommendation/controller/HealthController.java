package com.cnpm.careerrecommendation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller providing application liveness status for monitoring and test verification.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    public record HealthResponse(String status) {}

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }
}

package com.devtrack.analytics.controller;

import com.devtrack.analytics.dto.response.ReadinessResponse;
import com.devtrack.analytics.service.ReadinessService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-ANALYTICS-01, per 06_API_Specification.md's Phase 5 root. GET with one side effect: it upserts
 * today's snapshot so the trend has history. That write is idempotent (same day = same row), so
 * repeated GETs are safe.
 */
@RestController
@RequestMapping("/api/v1/analytics/readiness")
public class ReadinessController {

  private final ReadinessService readinessService;
  private final CurrentUserResolver currentUserResolver;

  public ReadinessController(
      ReadinessService readinessService, CurrentUserResolver currentUserResolver) {
    this.readinessService = readinessService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<ReadinessResponse> get() {
    return ApiEnvelope.success(
        readinessService.getReadiness(currentUserResolver.getCurrentUserId()));
  }
}

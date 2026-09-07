package com.devtrack.dsa.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.dsa.dto.request.CreateDsaAttemptRequest;
import com.devtrack.dsa.dto.response.DsaAttemptResponse;
import com.devtrack.dsa.service.DsaAttemptService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-DSA-01, per the Phase 2 planning pass. */
@RestController
@RequestMapping("/api/v1/dsa-problems/{problemId}/attempts")
public class DsaAttemptController {

  private final DsaAttemptService dsaAttemptService;
  private final CurrentUserResolver currentUserResolver;

  public DsaAttemptController(
      DsaAttemptService dsaAttemptService, CurrentUserResolver currentUserResolver) {
    this.dsaAttemptService = dsaAttemptService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<DsaAttemptResponse>> list(@PathVariable UUID problemId) {
    return ApiEnvelope.success(
        dsaAttemptService.listAttempts(problemId, currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<DsaAttemptResponse> create(
      @PathVariable UUID problemId, @Valid @RequestBody CreateDsaAttemptRequest request) {
    return ApiEnvelope.success(
        dsaAttemptService.logAttempt(problemId, currentUserResolver.getCurrentUserId(), request));
  }
}

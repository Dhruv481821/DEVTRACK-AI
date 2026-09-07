package com.devtrack.dsa.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.dsa.dto.request.CreateDsaProblemRequest;
import com.devtrack.dsa.dto.response.DsaProblemResponse;
import com.devtrack.dsa.dto.response.DsaTrendsResponse;
import com.devtrack.dsa.service.DsaProblemService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-DSA-01/02, per the Phase 2 planning pass. */
@RestController
@RequestMapping("/api/v1/dsa-problems")
public class DsaProblemController {

  private final DsaProblemService dsaProblemService;
  private final CurrentUserResolver currentUserResolver;

  public DsaProblemController(
      DsaProblemService dsaProblemService, CurrentUserResolver currentUserResolver) {
    this.dsaProblemService = dsaProblemService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<DsaProblemResponse>> list() {
    return ApiEnvelope.success(
        dsaProblemService.listMyProblems(currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<DsaProblemResponse> create(
      @Valid @RequestBody CreateDsaProblemRequest request) {
    return ApiEnvelope.success(
        dsaProblemService.createProblem(currentUserResolver.getCurrentUserId(), request));
  }

  @GetMapping("/trends")
  public ApiEnvelope<DsaTrendsResponse> trends() {
    return ApiEnvelope.success(dsaProblemService.getTrends(currentUserResolver.getCurrentUserId()));
  }
}

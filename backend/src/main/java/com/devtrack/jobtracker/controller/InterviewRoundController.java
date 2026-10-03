package com.devtrack.jobtracker.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.jobtracker.dto.request.CreateInterviewRoundRequest;
import com.devtrack.jobtracker.dto.request.UpdateInterviewRoundRequest;
import com.devtrack.jobtracker.dto.response.InterviewRoundResponse;
import com.devtrack.jobtracker.service.InterviewRoundService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-INT-01, per /docs/06_API_Specification.md §3's Phase 3 resource path
 * (.../job-applications/{id}/interview-rounds) — same nesting as ResumeSectionController.
 */
@RestController
@RequestMapping("/api/v1/job-applications/{applicationId}/interview-rounds")
public class InterviewRoundController {

  private final InterviewRoundService interviewRoundService;
  private final CurrentUserResolver currentUserResolver;

  public InterviewRoundController(
      InterviewRoundService interviewRoundService, CurrentUserResolver currentUserResolver) {
    this.interviewRoundService = interviewRoundService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<InterviewRoundResponse>> list(@PathVariable UUID applicationId) {
    return ApiEnvelope.success(
        interviewRoundService.listRoundsForApplication(
            applicationId, currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<InterviewRoundResponse> create(
      @PathVariable UUID applicationId, @Valid @RequestBody CreateInterviewRoundRequest request) {
    return ApiEnvelope.success(
        interviewRoundService.createRound(
            applicationId, currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{roundId}")
  public ApiEnvelope<InterviewRoundResponse> update(
      @PathVariable UUID applicationId,
      @PathVariable UUID roundId,
      @Valid @RequestBody UpdateInterviewRoundRequest request) {
    return ApiEnvelope.success(
        interviewRoundService.updateRound(
            applicationId, roundId, currentUserResolver.getCurrentUserId(), request));
  }
}

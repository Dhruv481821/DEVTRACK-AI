package com.devtrack.ai.controller;

import com.devtrack.ai.dto.request.AtsScoreRequest;
import com.devtrack.ai.dto.response.AtsScoreResponse;
import com.devtrack.ai.service.ResumeAtsService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-ATS-01. Lives alongside Resume's own controllers in the URL space but not in the module. */
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/ats-score")
public class ResumeAtsController {

  private final ResumeAtsService resumeAtsService;
  private final CurrentUserResolver currentUserResolver;

  public ResumeAtsController(
      ResumeAtsService resumeAtsService, CurrentUserResolver currentUserResolver) {
    this.resumeAtsService = resumeAtsService;
    this.currentUserResolver = currentUserResolver;
  }

  @PostMapping
  public ApiEnvelope<AtsScoreResponse> score(
      @PathVariable UUID resumeId, @Valid @RequestBody AtsScoreRequest request) {
    UUID userId = currentUserResolver.getCurrentUserId();
    return ApiEnvelope.success(
        resumeAtsService.scoreResume(userId, resumeId, request.jobDescription()));
  }
}

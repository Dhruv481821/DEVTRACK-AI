package com.devtrack.jobtracker.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.jobtracker.dto.request.CreateJobApplicationRequest;
import com.devtrack.jobtracker.dto.request.UpdateJobApplicationRequest;
import com.devtrack.jobtracker.dto.response.JobApplicationResponse;
import com.devtrack.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FR-JOB-01, per /docs/06_API_Specification.md §3's Phase 3 resource root
 * (/api/v1/job-applications). No GET /{id} — not needed yet; the kanban view works off the full
 * list (listMyApplications), same reasoning as Certificates not having one. Interview rounds
 * (FR-INT-01, .../job-applications/{id}/interview-rounds) are a separate slice, not in this batch.
 */
@RestController
@RequestMapping("/api/v1/job-applications")
public class JobApplicationController {

  private final JobApplicationService jobApplicationService;
  private final CurrentUserResolver currentUserResolver;

  public JobApplicationController(
      JobApplicationService jobApplicationService, CurrentUserResolver currentUserResolver) {
    this.jobApplicationService = jobApplicationService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<JobApplicationResponse>> list() {
    return ApiEnvelope.success(
        jobApplicationService.listMyApplications(currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<JobApplicationResponse> create(
      @Valid @RequestBody CreateJobApplicationRequest request) {
    return ApiEnvelope.success(
        jobApplicationService.create(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<JobApplicationResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateJobApplicationRequest request) {
    return ApiEnvelope.success(
        jobApplicationService.update(id, currentUserResolver.getCurrentUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    jobApplicationService.delete(id, currentUserResolver.getCurrentUserId());
    return ApiEnvelope.success(null);
  }
}

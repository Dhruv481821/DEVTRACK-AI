package com.devtrack.resume.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.resume.dto.request.CreateResumeRequest;
import com.devtrack.resume.dto.request.UpdateResumeRequest;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.service.ResumeService;
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

/** FR-RESUME-01, per /docs/06_API_Specification.md's resume endpoints. */
@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {
  private final ResumeService resumeService;
  private final CurrentUserResolver currentUserResolver;

  public ResumeController(ResumeService resumeService, CurrentUserResolver currentUserResolver) {
    this.resumeService = resumeService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<ResumeResponse>> list() {
    return ApiEnvelope.success(resumeService.listMyResumes(currentUserResolver.getCurrentUserId()));
  }

  @GetMapping("/{id}")
  public ApiEnvelope<ResumeResponse> get(@PathVariable UUID id) {
    return ApiEnvelope.success(
        resumeService.getMyResume(id, currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<ResumeResponse> create(@Valid @RequestBody CreateResumeRequest request) {
    return ApiEnvelope.success(
        resumeService.createResume(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<ResumeResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateResumeRequest request) {
    return ApiEnvelope.success(
        resumeService.updateResume(id, currentUserResolver.getCurrentUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    resumeService.deleteResume(id, currentUserResolver.getCurrentUserId());
    return ApiEnvelope.success(null);
  }
}

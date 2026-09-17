package com.devtrack.resume.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.resume.dto.request.CreateResumeSectionRequest;
import com.devtrack.resume.dto.request.UpdateResumeSectionRequest;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.service.ResumeSectionService;
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

/** FR-RESUME-01, per /docs/06_API_Specification.md's resume-section endpoints. */
@RestController
@RequestMapping("/api/v1/resumes/{resumeId}/sections")
public class ResumeSectionController {
  private final ResumeSectionService resumeSectionService;
  private final CurrentUserResolver currentUserResolver;

  public ResumeSectionController(
      ResumeSectionService resumeSectionService, CurrentUserResolver currentUserResolver) {
    this.resumeSectionService = resumeSectionService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<ResumeSectionResponse>> list(@PathVariable UUID resumeId) {
    return ApiEnvelope.success(
        resumeSectionService.listSectionsForResume(
            resumeId, currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<ResumeSectionResponse> create(
      @PathVariable UUID resumeId, @Valid @RequestBody CreateResumeSectionRequest request) {
    return ApiEnvelope.success(
        resumeSectionService.createSection(
            resumeId, currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{sectionId}")
  public ApiEnvelope<ResumeSectionResponse> update(
      @PathVariable UUID resumeId,
      @PathVariable UUID sectionId,
      @Valid @RequestBody UpdateResumeSectionRequest request) {
    return ApiEnvelope.success(
        resumeSectionService.updateSection(
            resumeId, sectionId, currentUserResolver.getCurrentUserId(), request));
  }
}

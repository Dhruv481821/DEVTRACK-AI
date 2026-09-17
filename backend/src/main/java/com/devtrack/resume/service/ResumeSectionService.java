package com.devtrack.resume.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.resume.dto.request.CreateResumeSectionRequest;
import com.devtrack.resume.dto.request.UpdateResumeSectionRequest;
import com.devtrack.resume.dto.response.ResumeSectionResponse;
import com.devtrack.resume.entity.Resume;
import com.devtrack.resume.entity.ResumeSection;
import com.devtrack.resume.repository.ResumeSectionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-RESUME-01. Reuses ResumeService.findOrThrow/assertOwned for ownership rather than duplicating
 * that logic — intra-module service composition, same pattern as StudyTaskService reusing
 * StudyPlanService.
 *
 * <p>sectionType is immutable after creation by design (see UpdateResumeSectionRequest's docblock)
 * — there is deliberately no way to change it here; create a new section instead of retyping an
 * existing one.
 *
 * <p>No delete endpoint — not part of the currently planned surface for this module (per the DTOs
 * already written). Add ResumeSectionRepository.deleteById + a soft-delete column here if that
 * changes; ResumeSection currently has no deleted_at, unlike Resume.
 */
@Service
public class ResumeSectionService {
  private final ResumeSectionRepository resumeSectionRepository;
  private final ResumeService resumeService;

  public ResumeSectionService(
      ResumeSectionRepository resumeSectionRepository, ResumeService resumeService) {
    this.resumeSectionRepository = resumeSectionRepository;
    this.resumeService = resumeService;
  }

  @Transactional(readOnly = true)
  public List<ResumeSectionResponse> listSectionsForResume(UUID resumeId, UUID userId) {
    Resume resume = resumeService.findOrThrow(resumeId);
    resumeService.assertOwned(resume, userId);
    return resumeSectionRepository.findByResumeIdOrderByOrderIndexAsc(resumeId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public ResumeSectionResponse createSection(
      UUID resumeId, UUID userId, CreateResumeSectionRequest request) {
    Resume resume = resumeService.findOrThrow(resumeId);
    resumeService.assertOwned(resume, userId);

    ResumeSection section = new ResumeSection();
    section.setResume(resume);
    section.setSectionType(request.sectionType());
    section.setContent(request.content() != null ? request.content() : Map.of());
    section.setOrderIndex(request.orderIndex());
    section.setCreatedAt(Instant.now());
    section.setUpdatedAt(Instant.now());

    resumeSectionRepository.save(section);
    return toResponse(section);
  }

  @Transactional
  public ResumeSectionResponse updateSection(
      UUID resumeId, UUID sectionId, UUID userId, UpdateResumeSectionRequest request) {
    Resume resume = resumeService.findOrThrow(resumeId);
    resumeService.assertOwned(resume, userId);

    ResumeSection section =
        resumeSectionRepository
            .findByIdAndResumeId(sectionId, resumeId)
            .orElseThrow(() -> new ResourceNotFoundException("Resume section not found."));

    if (request.content() != null) {
      section.setContent(request.content());
    }

    if (request.orderIndex() != null) {
      section.setOrderIndex(request.orderIndex());
    }

    section.setUpdatedAt(Instant.now());
    resumeSectionRepository.save(section);

    return toResponse(section);
  }

  private ResumeSectionResponse toResponse(ResumeSection section) {
    return new ResumeSectionResponse(
        section.getId(),
        section.getSectionType().name(),
        section.getContent(),
        section.getOrderIndex());
  }
}

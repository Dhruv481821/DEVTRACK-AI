package com.devtrack.resume.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.resume.dto.request.CreateResumeRequest;
import com.devtrack.resume.dto.request.UpdateResumeRequest;
import com.devtrack.resume.dto.response.ResumeResponse;
import com.devtrack.resume.entity.Resume;
import com.devtrack.resume.repository.ResumeRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-RESUME-01. Same CRUD + ownership + soft-delete pattern as every other Phase 1/2 module. */
@Service
public class ResumeService {

  private final ResumeRepository resumeRepository;
  private final OwnershipGuard ownershipGuard;

  public ResumeService(ResumeRepository resumeRepository, OwnershipGuard ownershipGuard) {
    this.resumeRepository = resumeRepository;
    this.ownershipGuard = ownershipGuard;
  }

  @Transactional(readOnly = true)
  public List<ResumeResponse> listMyResumes(UUID userId) {
    return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ResumeResponse getMyResume(UUID resumeId, UUID userId) {
    Resume resume = findOrThrow(resumeId);
    ownershipGuard.assertOwnedBy(resume.getUserId(), userId);
    return toResponse(resume);
  }

  @Transactional
  public ResumeResponse createResume(UUID userId, CreateResumeRequest request) {

    Resume resume = new Resume();
    resume.setUserId(userId);
    resume.setTitle(request.title());
    resume.setCreatedAt(Instant.now());
    resume.setUpdatedAt(Instant.now());

    resumeRepository.save(resume);

    return toResponse(resume);
  }

  @Transactional
  public ResumeResponse updateResume(UUID resumeId, UUID userId, UpdateResumeRequest request) {

    Resume resume = findOrThrow(resumeId);
    ownershipGuard.assertOwnedBy(resume.getUserId(), userId);

    if (request.title() != null) {
      resume.setTitle(request.title());
    }

    resume.setUpdatedAt(Instant.now());
    resumeRepository.save(resume);

    return toResponse(resume);
  }

  @Transactional
  public void deleteResume(UUID resumeId, UUID userId) {
    Resume resume = findOrThrow(resumeId);
    ownershipGuard.assertOwnedBy(resume.getUserId(), userId);

    resume.setDeletedAt(Instant.now());
    resumeRepository.save(resume);
  }

  Resume findOrThrow(UUID resumeId) {
    return resumeRepository
        .findById(resumeId)
        .orElseThrow(() -> new ResourceNotFoundException("Resume not found."));
  }

  void assertOwned(Resume resume, UUID userId) {
    ownershipGuard.assertOwnedBy(resume.getUserId(), userId);
  }

  private ResumeResponse toResponse(Resume resume) {
    return new ResumeResponse(
        resume.getId(), resume.getTitle(), resume.getCreatedAt(), resume.getUpdatedAt());
  }
}

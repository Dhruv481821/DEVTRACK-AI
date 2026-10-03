package com.devtrack.jobtracker.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.jobtracker.dto.request.CreateJobApplicationRequest;
import com.devtrack.jobtracker.dto.request.UpdateJobApplicationRequest;
import com.devtrack.jobtracker.dto.response.JobApplicationResponse;
import com.devtrack.jobtracker.entity.JobApplication;
import com.devtrack.jobtracker.repository.JobApplicationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-JOB-01. Same CRUD + ownership + soft-delete pattern as ResumeService. */
@Service
public class JobApplicationService {

  private final JobApplicationRepository jobApplicationRepository;
  private final OwnershipGuard ownershipGuard;

  public JobApplicationService(
      JobApplicationRepository jobApplicationRepository, OwnershipGuard ownershipGuard) {
    this.jobApplicationRepository = jobApplicationRepository;
    this.ownershipGuard = ownershipGuard;
  }

  @Transactional(readOnly = true)
  public List<JobApplicationResponse> listMyApplications(UUID userId) {
    return jobApplicationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public JobApplicationResponse create(UUID userId, CreateJobApplicationRequest request) {
    JobApplication application = new JobApplication();
    application.setUserId(userId);
    application.setCompanyName(request.companyName());
    application.setRoleTitle(request.roleTitle());
    application.setJobUrl(request.jobUrl());
    application.setNotes(request.notes());
    application.setCreatedAt(Instant.now());
    application.setUpdatedAt(Instant.now());

    jobApplicationRepository.save(application);

    return toResponse(application);
  }

  @Transactional
  public JobApplicationResponse update(
      UUID applicationId, UUID userId, UpdateJobApplicationRequest request) {
    JobApplication application = findOrThrow(applicationId);
    ownershipGuard.assertOwnedBy(application.getUserId(), userId);

    if (request.companyName() != null) {
      application.setCompanyName(request.companyName());
    }
    if (request.roleTitle() != null) {
      application.setRoleTitle(request.roleTitle());
    }
    if (request.jobUrl() != null) {
      application.setJobUrl(request.jobUrl());
    }
    if (request.notes() != null) {
      application.setNotes(request.notes());
    }
    if (request.stage() != null) {
      application.setStage(request.stage());
    }

    application.setUpdatedAt(Instant.now());
    jobApplicationRepository.save(application);

    return toResponse(application);
  }

  @Transactional
  public void delete(UUID applicationId, UUID userId) {
    JobApplication application = findOrThrow(applicationId);
    ownershipGuard.assertOwnedBy(application.getUserId(), userId);

    application.setDeletedAt(Instant.now());
    jobApplicationRepository.save(application);
  }

  private JobApplication findOrThrow(UUID applicationId) {
    return jobApplicationRepository
        .findById(applicationId)
        .orElseThrow(() -> new ResourceNotFoundException("Job application not found."));
  }

  private JobApplicationResponse toResponse(JobApplication application) {
    return new JobApplicationResponse(
        application.getId(),
        application.getCompanyName(),
        application.getRoleTitle(),
        application.getJobUrl(),
        application.getStage(),
        application.getNotes(),
        application.getCreatedAt(),
        application.getUpdatedAt());
  }
}
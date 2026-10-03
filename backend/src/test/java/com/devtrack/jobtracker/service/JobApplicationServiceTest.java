package com.devtrack.jobtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.jobtracker.dto.request.CreateJobApplicationRequest;
import com.devtrack.jobtracker.dto.request.UpdateJobApplicationRequest;
import com.devtrack.jobtracker.entity.JobApplication;
import com.devtrack.jobtracker.entity.JobApplicationStage;
import com.devtrack.jobtracker.repository.JobApplicationRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobApplicationServiceTest {

  private JobApplicationRepository jobApplicationRepository;
  private JobApplicationService jobApplicationService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    jobApplicationRepository = mock(JobApplicationRepository.class);

    jobApplicationService =
        new JobApplicationService(jobApplicationRepository, new OwnershipGuard());

    when(jobApplicationRepository.save(any(JobApplication.class)))
        .thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void create_defaultsToAppliedStage() {

    var response =
        jobApplicationService.create(
            ownerId, new CreateJobApplicationRequest("Acme Corp", "Backend Engineer", null, null));

    assertThat(response.companyName()).isEqualTo("Acme Corp");
    assertThat(response.stage()).isEqualTo(JobApplicationStage.APPLIED);
  }

  @Test
  void update_movesStage() {

    JobApplication application = existingApplication(ownerId);

    when(jobApplicationRepository.findById(application.getId()))
        .thenReturn(Optional.of(application));

    var response =
        jobApplicationService.update(
            application.getId(),
            ownerId,
            new UpdateJobApplicationRequest(null, null, null, null, JobApplicationStage.INTERVIEW));

    assertThat(response.stage()).isEqualTo(JobApplicationStage.INTERVIEW);
  }

  @Test
  void update_forNonOwner_throwsNotFound() {

    JobApplication application = existingApplication(ownerId);

    when(jobApplicationRepository.findById(application.getId()))
        .thenReturn(Optional.of(application));

    assertThatThrownBy(
            () ->
                jobApplicationService.update(
                    application.getId(),
                    otherUserId,
                    new UpdateJobApplicationRequest(null, null, null, null, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(jobApplicationRepository, never()).save(any());
  }

  @Test
  void delete_setsDeletedAtInsteadOfHardDeleting() {

    JobApplication application = existingApplication(ownerId);

    when(jobApplicationRepository.findById(application.getId()))
        .thenReturn(Optional.of(application));

    jobApplicationService.delete(application.getId(), ownerId);

    assertThat(application.getDeletedAt()).isNotNull();

    verify(jobApplicationRepository, never()).deleteById(any());
  }

  @Test
  void delete_forNonOwner_throwsNotFoundAndDoesNotDelete() {

    JobApplication application = existingApplication(ownerId);

    when(jobApplicationRepository.findById(application.getId()))
        .thenReturn(Optional.of(application));

    assertThatThrownBy(() -> jobApplicationService.delete(application.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);

    assertThat(application.getDeletedAt()).isNull();
  }

  private JobApplication existingApplication(UUID ownerId) {

    JobApplication application = new JobApplication();

    application.setId(UUID.randomUUID());
    application.setUserId(ownerId);
    application.setCompanyName("A company");
    application.setRoleTitle("A role");
    application.setStage(JobApplicationStage.APPLIED);

    return application;
  }
}

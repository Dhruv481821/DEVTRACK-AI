package com.devtrack.jobtracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.jobtracker.dto.request.CreateInterviewRoundRequest;
import com.devtrack.jobtracker.dto.request.UpdateInterviewRoundRequest;
import com.devtrack.jobtracker.entity.InterviewOutcome;
import com.devtrack.jobtracker.entity.InterviewRound;
import com.devtrack.jobtracker.entity.JobApplication;
import com.devtrack.jobtracker.repository.InterviewRoundRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterviewRoundServiceTest {

  private InterviewRoundRepository interviewRoundRepository;
  private JobApplicationService jobApplicationService;
  private InterviewRoundService interviewRoundService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    interviewRoundRepository = mock(InterviewRoundRepository.class);
    jobApplicationService = mock(JobApplicationService.class);
    interviewRoundService =
        new InterviewRoundService(interviewRoundRepository, jobApplicationService);

    when(interviewRoundRepository.save(any(InterviewRound.class)))
        .thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void listRoundsForApplication_forNonOwner_throwsNotFound() {
    JobApplication application = existingApplication(ownerId);

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doThrow(new ResourceNotFoundException("Job application not found."))
        .when(jobApplicationService)
        .assertOwned(application, otherUserId);

    assertThatThrownBy(
            () -> interviewRoundService.listRoundsForApplication(application.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createRound_defaultsToPendingOutcome() {
    JobApplication application = existingApplication(ownerId);

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doNothing().when(jobApplicationService).assertOwned(application, ownerId);

    var response =
        interviewRoundService.createRound(
            application.getId(),
            ownerId,
            new CreateInterviewRoundRequest("Phone Screen", Instant.now(), null));

    assertThat(response.roundName()).isEqualTo("Phone Screen");
    assertThat(response.outcome()).isEqualTo(InterviewOutcome.PENDING);
  }

  @Test
  void createRound_forNonOwner_throwsNotFoundAndDoesNotSave() {
    JobApplication application = existingApplication(ownerId);

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doThrow(new ResourceNotFoundException("Job application not found."))
        .when(jobApplicationService)
        .assertOwned(application, otherUserId);

    assertThatThrownBy(
            () ->
                interviewRoundService.createRound(
                    application.getId(),
                    otherUserId,
                    new CreateInterviewRoundRequest("Onsite", null, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(interviewRoundRepository, never()).save(any());
  }

  @Test
  void updateRound_setsOutcome() {
    JobApplication application = existingApplication(ownerId);
    InterviewRound round = existingRound(application);

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doNothing().when(jobApplicationService).assertOwned(application, ownerId);
    when(interviewRoundRepository.findByIdAndJobApplicationId(round.getId(), application.getId()))
        .thenReturn(Optional.of(round));

    var response =
        interviewRoundService.updateRound(
            application.getId(),
            round.getId(),
            ownerId,
            new UpdateInterviewRoundRequest(null, null, InterviewOutcome.PASSED, null));

    assertThat(response.outcome()).isEqualTo(InterviewOutcome.PASSED);
  }

  @Test
  void updateRound_whenRoundDoesNotBelongToGivenApplication_throwsNotFound() {
    JobApplication application = existingApplication(ownerId);
    UUID mismatchedRoundId = UUID.randomUUID();

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doNothing().when(jobApplicationService).assertOwned(application, ownerId);
    when(interviewRoundRepository.findByIdAndJobApplicationId(
            mismatchedRoundId, application.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                interviewRoundService.updateRound(
                    application.getId(),
                    mismatchedRoundId,
                    ownerId,
                    new UpdateInterviewRoundRequest(null, null, null, null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void listRoundsForApplication_returnsRoundsInRepositoryOrder() {
    JobApplication application = existingApplication(ownerId);
    InterviewRound round = existingRound(application);

    when(jobApplicationService.findOrThrow(application.getId())).thenReturn(application);
    doNothing().when(jobApplicationService).assertOwned(application, ownerId);
    when(interviewRoundRepository.findByJobApplicationIdOrderByOccurredAtAsc(application.getId()))
        .thenReturn(List.of(round));

    var responses = interviewRoundService.listRoundsForApplication(application.getId(), ownerId);

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).roundName()).isEqualTo("Phone Screen");
  }

  private JobApplication existingApplication(UUID userId) {
    JobApplication application = new JobApplication();
    application.setId(UUID.randomUUID());
    application.setUserId(userId);
    application.setCompanyName("A company");
    application.setRoleTitle("A role");
    return application;
  }

  private InterviewRound existingRound(JobApplication application) {
    InterviewRound round = new InterviewRound();
    round.setId(UUID.randomUUID());
    round.setJobApplication(application);
    round.setRoundName("Phone Screen");
    round.setOutcome(InterviewOutcome.PENDING);
    return round;
  }
}

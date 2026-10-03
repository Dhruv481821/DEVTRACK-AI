package com.devtrack.jobtracker.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.jobtracker.dto.request.CreateInterviewRoundRequest;
import com.devtrack.jobtracker.dto.request.UpdateInterviewRoundRequest;
import com.devtrack.jobtracker.dto.response.InterviewRoundResponse;
import com.devtrack.jobtracker.entity.InterviewRound;
import com.devtrack.jobtracker.entity.JobApplication;
import com.devtrack.jobtracker.repository.InterviewRoundRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-INT-01. Reuses JobApplicationService.findOrThrow/assertOwned for ownership rather than
 * duplicating that logic — same intra-module composition as ResumeSectionService reusing
 * ResumeService.
 *
 * <p>No delete endpoint — same "not part of the currently planned surface" call made for resume
 * sections (ResumeSectionService's docblock).
 */
@Service
public class InterviewRoundService {

  private final InterviewRoundRepository interviewRoundRepository;
  private final JobApplicationService jobApplicationService;

  public InterviewRoundService(
      InterviewRoundRepository interviewRoundRepository,
      JobApplicationService jobApplicationService) {
    this.interviewRoundRepository = interviewRoundRepository;
    this.jobApplicationService = jobApplicationService;
  }

  @Transactional(readOnly = true)
  public List<InterviewRoundResponse> listRoundsForApplication(UUID applicationId, UUID userId) {
    JobApplication application = jobApplicationService.findOrThrow(applicationId);
    jobApplicationService.assertOwned(application, userId);

    return interviewRoundRepository
        .findByJobApplicationIdOrderByOccurredAtAsc(applicationId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public InterviewRoundResponse createRound(
      UUID applicationId, UUID userId, CreateInterviewRoundRequest request) {
    JobApplication application = jobApplicationService.findOrThrow(applicationId);
    jobApplicationService.assertOwned(application, userId);

    InterviewRound round = new InterviewRound();
    round.setJobApplication(application);
    round.setRoundName(request.roundName());
    round.setOccurredAt(request.occurredAt());
    round.setNotes(request.notes());
    round.setCreatedAt(Instant.now());
    round.setUpdatedAt(Instant.now());

    interviewRoundRepository.save(round);
    return toResponse(round);
  }

  @Transactional
  public InterviewRoundResponse updateRound(
      UUID applicationId, UUID roundId, UUID userId, UpdateInterviewRoundRequest request) {
    JobApplication application = jobApplicationService.findOrThrow(applicationId);
    jobApplicationService.assertOwned(application, userId);

    InterviewRound round =
        interviewRoundRepository
            .findByIdAndJobApplicationId(roundId, applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview round not found."));

    if (request.roundName() != null) {
      round.setRoundName(request.roundName());
    }
    if (request.occurredAt() != null) {
      round.setOccurredAt(request.occurredAt());
    }
    if (request.outcome() != null) {
      round.setOutcome(request.outcome());
    }
    if (request.notes() != null) {
      round.setNotes(request.notes());
    }

    round.setUpdatedAt(Instant.now());
    interviewRoundRepository.save(round);

    return toResponse(round);
  }

  private InterviewRoundResponse toResponse(InterviewRound round) {
    return new InterviewRoundResponse(
        round.getId(),
        round.getRoundName(),
        round.getOccurredAt(),
        round.getOutcome(),
        round.getNotes(),
        round.getCreatedAt(),
        round.getUpdatedAt());
  }
}

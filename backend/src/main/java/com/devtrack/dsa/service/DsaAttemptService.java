package com.devtrack.dsa.service;

import com.devtrack.dsa.dto.request.CreateDsaAttemptRequest;
import com.devtrack.dsa.dto.response.DsaAttemptResponse;
import com.devtrack.dsa.entity.DsaAttempt;
import com.devtrack.dsa.entity.DsaProblem;
import com.devtrack.dsa.repository.DsaAttemptRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-DSA-01. Reuses DsaProblemService for problem lookup and ownership checking. */
@Service
public class DsaAttemptService {

  private final DsaAttemptRepository dsaAttemptRepository;
  private final DsaProblemService dsaProblemService;

  public DsaAttemptService(
      DsaAttemptRepository dsaAttemptRepository, DsaProblemService dsaProblemService) {
    this.dsaAttemptRepository = dsaAttemptRepository;
    this.dsaProblemService = dsaProblemService;
  }

  @Transactional(readOnly = true)
  public List<DsaAttemptResponse> listAttempts(UUID problemId, UUID userId) {

    DsaProblem problem = dsaProblemService.findOrThrow(problemId);

    dsaProblemService.assertOwned(problem, userId);

    return dsaAttemptRepository.findByDsaProblemId(problemId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public DsaAttemptResponse logAttempt(
      UUID problemId, UUID userId, CreateDsaAttemptRequest request) {

    DsaProblem problem = dsaProblemService.findOrThrow(problemId);

    dsaProblemService.assertOwned(problem, userId);

    DsaAttempt attempt = new DsaAttempt();

    attempt.setDsaProblem(problem);
    attempt.setAttemptedAt(request.attemptedAt());
    attempt.setTimeTakenMinutes(request.timeTakenMinutes());
    attempt.setNotes(request.notes());
    attempt.setCreatedAt(Instant.now());

    dsaAttemptRepository.save(attempt);

    return toResponse(attempt);
  }

  private DsaAttemptResponse toResponse(DsaAttempt attempt) {

    return new DsaAttemptResponse(
        attempt.getId(),
        attempt.getAttemptedAt(),
        attempt.getTimeTakenMinutes(),
        attempt.getNotes());
  }
}

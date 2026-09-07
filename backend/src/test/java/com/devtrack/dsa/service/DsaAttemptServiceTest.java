package com.devtrack.dsa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.dsa.dto.request.CreateDsaAttemptRequest;
import com.devtrack.dsa.entity.Difficulty;
import com.devtrack.dsa.entity.DsaAttempt;
import com.devtrack.dsa.entity.DsaProblem;
import com.devtrack.dsa.repository.DsaAttemptRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DsaAttemptServiceTest {

  private DsaAttemptRepository dsaAttemptRepository;
  private DsaProblemService dsaProblemService;
  private DsaAttemptService dsaAttemptService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    dsaAttemptRepository = mock(DsaAttemptRepository.class);
    dsaProblemService = mock(DsaProblemService.class);

    dsaAttemptService = new DsaAttemptService(dsaAttemptRepository, dsaProblemService);

    when(dsaAttemptRepository.save(any(DsaAttempt.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void logAttempt_forOwner_succeeds() {
    DsaProblem problem = existingProblem();

    when(dsaProblemService.findOrThrow(problem.getId())).thenReturn(problem);

    var response =
        dsaAttemptService.logAttempt(
            problem.getId(),
            ownerId,
            new CreateDsaAttemptRequest(LocalDate.now(), 25, "Used two pointers"));

    assertThat(response.timeTakenMinutes()).isEqualTo(25);

    verify(dsaProblemService).assertOwned(problem, ownerId);
  }

  /**
   * Reuses DsaProblemService.assertOwned — this confirms the reuse actually enforces the rejection,
   * not just compiles.
   */
  @Test
  void logAttempt_forNonOwner_throwsAndDoesNotSave() {
    DsaProblem problem = existingProblem();

    when(dsaProblemService.findOrThrow(problem.getId())).thenReturn(problem);

    doThrow(new ResourceNotFoundException("Problem not found."))
        .when(dsaProblemService)
        .assertOwned(problem, otherUserId);

    assertThatThrownBy(
            () ->
                dsaAttemptService.logAttempt(
                    problem.getId(),
                    otherUserId,
                    new CreateDsaAttemptRequest(LocalDate.now(), null, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(dsaAttemptRepository, never()).save(any());
  }

  private DsaProblem existingProblem() {
    DsaProblem problem = new DsaProblem();

    problem.setId(UUID.randomUUID());
    problem.setUserId(ownerId);
    problem.setTitle("A problem");
    problem.setDifficulty(Difficulty.MEDIUM);

    return problem;
  }
}

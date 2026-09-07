package com.devtrack.dsa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.dsa.dto.request.CreateDsaProblemRequest;
import com.devtrack.dsa.entity.Difficulty;
import com.devtrack.dsa.entity.DsaProblem;
import com.devtrack.dsa.repository.DsaProblemRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DsaProblemServiceTest {

  private DsaProblemRepository dsaProblemRepository;
  private DsaProblemService dsaProblemService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    dsaProblemRepository = mock(DsaProblemRepository.class);
    dsaProblemService = new DsaProblemService(dsaProblemRepository, new OwnershipGuard());

    when(dsaProblemRepository.save(any(DsaProblem.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void createProblem_savesWithGivenFields() {
    var response =
        dsaProblemService.createProblem(
            ownerId,
            new CreateDsaProblemRequest("Two Sum", Difficulty.EASY, List.of("arrays", "hashmap")));

    assertThat(response.title()).isEqualTo("Two Sum");
    assertThat(response.tags()).containsExactly("arrays", "hashmap");
  }

  /**
   * The actual point of this whole module's FR-DSA-02 requirement — verified with real numbers, not
   * just "it runs."
   */
  @Test
  void getTrends_computesDifficultyDistributionAndTagFrequencyCorrectly() {
    when(dsaProblemRepository.findByUserIdOrderByCreatedAtDesc(ownerId))
        .thenReturn(
            List.of(
                problem(Difficulty.EASY, "arrays", "hashmap"),
                problem(Difficulty.EASY, "arrays"),
                problem(Difficulty.HARD, "dp")));

    var trends = dsaProblemService.getTrends(ownerId);

    assertThat(trends.totalProblems()).isEqualTo(3);

    assertThat(trends.difficultyDistribution()).containsEntry("EASY", 2L).containsEntry("HARD", 1L);

    assertThat(trends.tagFrequency())
        .containsEntry("arrays", 2L)
        .containsEntry("hashmap", 1L)
        .containsEntry("dp", 1L);
  }

  @Test
  void assertOwned_forNonOwner_throws() {
    DsaProblem problem = problem(Difficulty.MEDIUM, "graphs");

    assertThatThrownBy(() -> dsaProblemService.assertOwned(problem, otherUserId))
        .isInstanceOf(com.devtrack.common.exception.ResourceNotFoundException.class);
  }

  private DsaProblem problem(Difficulty difficulty, String... tags) {
    DsaProblem problem = new DsaProblem();

    problem.setId(UUID.randomUUID());
    problem.setUserId(ownerId);
    problem.setTitle("A problem");
    problem.setDifficulty(difficulty);
    problem.setTags(List.of(tags));

    return problem;
  }
}

package com.devtrack.dsa.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.dsa.dto.request.CreateDsaProblemRequest;
import com.devtrack.dsa.dto.response.DsaProblemResponse;
import com.devtrack.dsa.dto.response.DsaTrendsResponse;
import com.devtrack.dsa.entity.DsaProblem;
import com.devtrack.dsa.repository.DsaProblemRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-DSA-01/02. */
@Service
public class DsaProblemService {

  private final DsaProblemRepository dsaProblemRepository;
  private final OwnershipGuard ownershipGuard;

  public DsaProblemService(
      DsaProblemRepository dsaProblemRepository, OwnershipGuard ownershipGuard) {
    this.dsaProblemRepository = dsaProblemRepository;
    this.ownershipGuard = ownershipGuard;
  }

  @Transactional(readOnly = true)
  public List<DsaProblemResponse> listMyProblems(UUID userId) {
    return dsaProblemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public DsaProblemResponse createProblem(UUID userId, CreateDsaProblemRequest request) {

    DsaProblem problem = new DsaProblem();

    problem.setUserId(userId);
    problem.setTitle(request.title());
    problem.setDifficulty(request.difficulty());
    problem.setTags(request.tags() != null ? request.tags() : List.of());

    Instant now = Instant.now();
    problem.setCreatedAt(now);
    problem.setUpdatedAt(now);

    dsaProblemRepository.save(problem);

    return toResponse(problem);
  }

  /** FR-DSA-02 — difficulty distribution and tag frequency. */
  @Transactional(readOnly = true)
  public DsaTrendsResponse getTrends(UUID userId) {

    List<DsaProblem> problems = dsaProblemRepository.findByUserIdOrderByCreatedAtDesc(userId);

    var difficultyDistribution =
        problems.stream()
            .collect(Collectors.groupingBy(p -> p.getDifficulty().name(), Collectors.counting()));

    var tagFrequency =
        problems.stream()
            .flatMap(p -> p.getTags().stream())
            .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

    return new DsaTrendsResponse(problems.size(), difficultyDistribution, tagFrequency);
  }

  DsaProblem findOrThrow(UUID problemId) {
    return dsaProblemRepository
        .findById(problemId)
        .orElseThrow(() -> new ResourceNotFoundException("Problem not found."));
  }

  void assertOwned(DsaProblem problem, UUID userId) {
    ownershipGuard.assertOwnedBy(problem.getUserId(), userId);
  }

  private DsaProblemResponse toResponse(DsaProblem problem) {
    return new DsaProblemResponse(
        problem.getId(),
        problem.getTitle(),
        problem.getDifficulty().name(),
        problem.getTags(),
        problem.getCreatedAt());
  }
}

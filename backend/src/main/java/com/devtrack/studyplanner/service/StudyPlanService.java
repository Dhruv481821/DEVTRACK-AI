// backend/src/main/java/com/devtrack/studyplanner/service/StudyPlanService.java
package com.devtrack.studyplanner.service;

import com.devtrack.common.events.StudyPlanTargetDateSetEvent;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.studyplanner.dto.request.CreateStudyPlanRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyPlanRequest;
import com.devtrack.studyplanner.dto.response.StudyPlanResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.repository.StudyPlanRepository;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership now goes through the shared OwnershipGuard (findOrThrow + assertOwned), same as every
 * other module. The previous findByIdAndUserId(...).orElseThrow() query-scoped approach threw a
 * bare NoSuchElementException on a missing/non-owned plan — GlobalExceptionHandler has no mapping
 * for that, so it fell through to the catch-all handler and returned 500 instead of 404.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudyPlanService {

  private final StudyPlanRepository studyPlanRepository;
  private final StudyTaskRepository studyTaskRepository;
  private final OwnershipGuard ownershipGuard;
  private final ApplicationEventPublisher eventPublisher;

  public StudyPlanResponse createPlan(UUID userId, CreateStudyPlanRequest request) {

    StudyPlan plan = new StudyPlan(userId, request.title(), request.targetDate());

    StudyPlan saved = studyPlanRepository.save(plan);

    publishTargetDateEventIfSet(saved);

    return mapPlan(saved);
  }

  @Transactional(readOnly = true)
  public List<StudyPlanResponse> getPlans(UUID userId) {
    return studyPlanRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapPlan)
        .toList();
  }

  public StudyPlanResponse updatePlan(UUID userId, UUID planId, UpdateStudyPlanRequest request) {

    StudyPlan plan = findOrThrow(planId);
    assertOwned(plan, userId);

    plan.update(request.title(), request.targetDate());

    StudyPlan saved = studyPlanRepository.save(plan);

    publishTargetDateEventIfSet(saved);

    return mapPlan(saved);
  }

  public void deletePlan(UUID userId, UUID planId) {
    StudyPlan plan = findOrThrow(planId);
    assertOwned(plan, userId);

    plan.softDelete();

    studyPlanRepository.save(plan);
  }

  StudyPlan findOrThrow(UUID planId) {
    return studyPlanRepository
        .findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Study plan not found."));
  }

  void assertOwned(StudyPlan plan, UUID userId) {
    ownershipGuard.assertOwnedBy(plan.getUserId(), userId);
  }

  /**
   * FR-PLAN's Calendar-sync half: fires only when targetDate ends up non-null, matching
   * StudyPlanEventListener's expectations. Known gap, not silently "handled": clearing a plan's
   * targetDate does not currently remove its linked calendar event.
   */
  private void publishTargetDateEventIfSet(StudyPlan plan) {
    if (plan.getTargetDate() != null) {
      eventPublisher.publishEvent(
          new StudyPlanTargetDateSetEvent(
              plan.getId(), plan.getUserId(), plan.getTitle(), plan.getTargetDate()));
    }
  }

  private StudyPlanResponse mapPlan(StudyPlan plan) {
    long totalTasks = studyTaskRepository.countByStudyPlanId(plan.getId());

    long completedTasks = studyTaskRepository.countByStudyPlanIdAndCompletedTrue(plan.getId());

    return new StudyPlanResponse(
        plan.getId(),
        plan.getTitle(),
        plan.getTargetDate(),
        totalTasks,
        completedTasks,
        plan.getCreatedAt(),
        plan.getUpdatedAt());
  }
}

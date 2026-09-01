package com.devtrack.studyplanner.service;

import com.devtrack.studyplanner.dto.request.CreateStudyPlanRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyPlanRequest;
import com.devtrack.studyplanner.dto.response.StudyPlanResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.repository.StudyPlanRepository;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyPlanService {

  private final StudyPlanRepository studyPlanRepository;
  private final StudyTaskRepository studyTaskRepository;

  public StudyPlanResponse createPlan(UUID userId, CreateStudyPlanRequest request) {

    StudyPlan plan = new StudyPlan(userId, request.title(), request.targetDate());

    StudyPlan saved = studyPlanRepository.save(plan);

    return mapPlan(saved);
  }

  @Transactional(readOnly = true)
  public List<StudyPlanResponse> getPlans(UUID userId) {
    return studyPlanRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapPlan)
        .toList();
  }

  public StudyPlanResponse updatePlan(UUID userId, UUID planId, UpdateStudyPlanRequest request) {

    StudyPlan plan = studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    plan.update(request.title(), request.targetDate());

    return mapPlan(studyPlanRepository.save(plan));
  }

  public void deletePlan(UUID userId, UUID planId) {
    StudyPlan plan = studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    plan.softDelete();

    studyPlanRepository.save(plan);
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

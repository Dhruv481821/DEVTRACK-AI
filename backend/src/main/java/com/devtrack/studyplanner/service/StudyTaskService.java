// backend/src/main/java/com/devtrack/studyplanner/service/StudyTaskService.java
package com.devtrack.studyplanner.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.studyplanner.dto.request.CreateStudyTaskRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyTaskRequest;
import com.devtrack.studyplanner.dto.response.StudyTaskResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.entity.StudyTask;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reuses StudyPlanService.findOrThrow/assertOwned for plan ownership rather than duplicating that
 * logic — same intra-module composition pattern as ResumeSectionService reusing ResumeService.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StudyTaskService {

  private final StudyTaskRepository studyTaskRepository;
  private final StudyPlanService studyPlanService;
  private final StudyStreakService studyStreakService;

  public StudyTaskResponse createTask(UUID userId, UUID planId, CreateStudyTaskRequest request) {

    StudyPlan plan = studyPlanService.findOrThrow(planId);
    studyPlanService.assertOwned(plan, userId);

    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    task.setTitle(request.title());

    if (request.dueDate() != null) {
      task.setDueDate(request.dueDate());
    }

    task.setCompleted(false);

    StudyTask saved = studyTaskRepository.save(task);

    studyStreakService.recordActivity(userId);

    return mapTask(saved);
  }

  @Transactional(readOnly = true)
  public List<StudyTaskResponse> getTasks(UUID userId, UUID planId) {

    StudyPlan plan = studyPlanService.findOrThrow(planId);
    studyPlanService.assertOwned(plan, userId);

    return studyTaskRepository.findAllByStudyPlanIdOrderByCreatedAtAsc(planId).stream()
        .map(this::mapTask)
        .toList();
  }

  public StudyTaskResponse updateTask(
      UUID userId, UUID planId, UUID taskId, UpdateStudyTaskRequest request) {

    StudyPlan plan = studyPlanService.findOrThrow(planId);
    studyPlanService.assertOwned(plan, userId);

    StudyTask task = findTaskOrThrow(taskId, planId);

    if (request.title() != null) {
      task.setTitle(request.title());
    }

    if (request.dueDate() != null) {
      task.setDueDate(request.dueDate());
    }

    if (request.completed() != null) {
      task.setCompleted(request.completed());
      if (request.completed()) {
        studyStreakService.recordActivity(userId);
      }
    }

    return mapTask(studyTaskRepository.save(task));
  }

  public void deleteTask(UUID userId, UUID planId, UUID taskId) {

    StudyPlan plan = studyPlanService.findOrThrow(planId);
    studyPlanService.assertOwned(plan, userId);

    StudyTask task = findTaskOrThrow(taskId, planId);

    studyTaskRepository.delete(task);
  }

  private StudyTask findTaskOrThrow(UUID taskId, UUID planId) {
    return studyTaskRepository
        .findByIdAndStudyPlanId(taskId, planId)
        .orElseThrow(() -> new ResourceNotFoundException("Study task not found."));
  }

  private StudyTaskResponse mapTask(StudyTask task) {
    return new StudyTaskResponse(
        task.getId(),
        task.getStudyPlan().getId(),
        task.getTitle(),
        task.isCompleted(),
        task.getDueDate(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}

package com.devtrack.studyplanner.service;

import com.devtrack.studyplanner.dto.request.CreateStudyTaskRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyTaskRequest;
import com.devtrack.studyplanner.dto.response.StudyTaskResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.entity.StudyTask;
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
public class StudyTaskService {

  private final StudyPlanRepository studyPlanRepository;
  private final StudyTaskRepository studyTaskRepository;

  public StudyTaskResponse createTask(UUID userId, UUID planId, CreateStudyTaskRequest request) {

    StudyPlan plan = studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    task.setTitle(request.title());

    if (request.dueDate() != null) {
      task.setDueDate(request.dueDate());
    }

    task.setCompleted(false);

    return mapTask(studyTaskRepository.save(task));
  }

  @Transactional(readOnly = true)
  public List<StudyTaskResponse> getTasks(UUID userId, UUID planId) {

    studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    return studyTaskRepository.findAllByStudyPlanIdOrderByCreatedAtAsc(planId).stream()
        .map(this::mapTask)
        .toList();
  }

  public StudyTaskResponse updateTask(
      UUID userId, UUID planId, UUID taskId, UpdateStudyTaskRequest request) {

    studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    StudyTask task = studyTaskRepository.findByIdAndStudyPlanId(taskId, planId).orElseThrow();

    if (request.title() != null) {
      task.setTitle(request.title());
    }

    if (request.dueDate() != null) {
      task.setDueDate(request.dueDate());
    }

    if (request.completed() != null) {
      task.setCompleted(request.completed());
    }

    return mapTask(studyTaskRepository.save(task));
  }

  public void deleteTask(UUID userId, UUID planId, UUID taskId) {

    studyPlanRepository.findByIdAndUserId(planId, userId).orElseThrow();

    StudyTask task = studyTaskRepository.findByIdAndStudyPlanId(taskId, planId).orElseThrow();

    studyTaskRepository.delete(task);
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

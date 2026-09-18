// backend/src/test/java/com/devtrack/studyplanner/service/StudyTaskServiceTest.java
package com.devtrack.studyplanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.studyplanner.dto.request.CreateStudyTaskRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyTaskRequest;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.entity.StudyTask;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudyTaskServiceTest {

  private StudyTaskRepository studyTaskRepository;
  private StudyPlanService studyPlanService;
  private StudyStreakService studyStreakService;
  private StudyTaskService studyTaskService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();
  private final UUID planId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    studyTaskRepository = mock(StudyTaskRepository.class);
    studyPlanService = mock(StudyPlanService.class);
    studyStreakService = mock(StudyStreakService.class);
    studyTaskService =
        new StudyTaskService(studyTaskRepository, studyPlanService, studyStreakService);
    when(studyTaskRepository.save(any(StudyTask.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  private StudyPlan ownedPlan() {
    return new StudyPlan(ownerId, "Java DSA", LocalDate.of(2026, 12, 31));
  }

  @Test
  void createTask_forNonOwner_throwsNotFoundAndDoesNotSave() {
    StudyPlan plan = ownedPlan();
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    doThrow(new ResourceNotFoundException("Study plan not found."))
        .when(studyPlanService)
        .assertOwned(plan, otherUserId);

    assertThatThrownBy(
            () ->
                studyTaskService.createTask(
                    otherUserId, planId, new CreateStudyTaskRequest("Arrays", null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(studyTaskRepository, never()).save(any());
    verifyNoInteractions(studyStreakService);
  }

  @Test
  void createTask_recordsStreakActivity() {
    StudyPlan plan = ownedPlan();
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);

    var response =
        studyTaskService.createTask(
            ownerId,
            planId,
            new CreateStudyTaskRequest("Complete Arrays", LocalDate.of(2026, 9, 10)));

    assertThat(response.title()).isEqualTo("Complete Arrays");
    assertThat(response.completed()).isFalse();
    verify(studyStreakService).recordActivity(ownerId);
  }

  @Test
  void getTasks_forNonOwner_throwsNotFound() {
    StudyPlan plan = ownedPlan();
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    doThrow(new ResourceNotFoundException("Study plan not found."))
        .when(studyPlanService)
        .assertOwned(plan, otherUserId);

    assertThatThrownBy(() -> studyTaskService.getTasks(otherUserId, planId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateTask_whenTaskDoesNotBelongToGivenPlan_throwsNotFound() {
    StudyPlan plan = ownedPlan();
    UUID taskId = UUID.randomUUID();
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    when(studyTaskRepository.findByIdAndStudyPlanId(taskId, planId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                studyTaskService.updateTask(
                    ownerId, planId, taskId, new UpdateStudyTaskRequest(null, null, null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateTask_markingCompleted_recordsStreakActivity() {
    StudyPlan plan = ownedPlan();
    UUID taskId = UUID.randomUUID();
    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    task.setTitle("Complete Arrays");
    task.setCompleted(false);
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    when(studyTaskRepository.findByIdAndStudyPlanId(taskId, planId)).thenReturn(Optional.of(task));

    var response =
        studyTaskService.updateTask(
            ownerId, planId, taskId, new UpdateStudyTaskRequest(null, null, true));

    assertThat(response.completed()).isTrue();
    verify(studyStreakService).recordActivity(ownerId);
  }

  @Test
  void updateTask_notTouchingCompletion_doesNotRecordStreakActivity() {
    StudyPlan plan = ownedPlan();
    UUID taskId = UUID.randomUUID();
    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    task.setTitle("Complete Arrays");
    task.setCompleted(false);
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    when(studyTaskRepository.findByIdAndStudyPlanId(taskId, planId)).thenReturn(Optional.of(task));

    studyTaskService.updateTask(
        ownerId, planId, taskId, new UpdateStudyTaskRequest("Renamed", null, null));

    verifyNoInteractions(studyStreakService);
  }

  @Test
  void deleteTask_removesTask() {
    StudyPlan plan = ownedPlan();
    UUID taskId = UUID.randomUUID();
    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    when(studyPlanService.findOrThrow(planId)).thenReturn(plan);
    when(studyTaskRepository.findByIdAndStudyPlanId(taskId, planId)).thenReturn(Optional.of(task));

    studyTaskService.deleteTask(ownerId, planId, taskId);

    verify(studyTaskRepository).delete(task);
  }
}

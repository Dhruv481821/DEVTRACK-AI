package com.devtrack.studyplanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.devtrack.studyplanner.dto.request.CreateStudyTaskRequest;
import com.devtrack.studyplanner.dto.response.StudyTaskResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.entity.StudyTask;
import com.devtrack.studyplanner.repository.StudyPlanRepository;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyTaskServiceTest {

  @Mock private StudyPlanRepository studyPlanRepository;

  @Mock private StudyTaskRepository studyTaskRepository;

  @InjectMocks private StudyTaskService studyTaskService;

  @Test
  void createTask_createsStudyTask() {
    UUID userId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();

    StudyPlan plan = new StudyPlan(userId, "Java DSA", LocalDate.of(2026, 12, 31));

    CreateStudyTaskRequest request =
        new CreateStudyTaskRequest("Complete Arrays", LocalDate.of(2026, 9, 10));

    StudyTask task = new StudyTask();
    task.setStudyPlan(plan);
    task.setTitle(request.title());
    task.setDueDate(request.dueDate());
    task.setCompleted(false);

    when(studyPlanRepository.findByIdAndUserId(planId, userId))
        .thenReturn(java.util.Optional.of(plan));

    when(studyTaskRepository.save(any(StudyTask.class))).thenReturn(task);

    StudyTaskResponse response = studyTaskService.createTask(userId, planId, request);

    assertEquals("Complete Arrays", response.title());
    assertEquals(LocalDate.of(2026, 9, 10), response.dueDate());
    assertEquals(false, response.completed());
  }
}

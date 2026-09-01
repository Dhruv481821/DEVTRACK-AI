package com.devtrack.studyplanner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.devtrack.studyplanner.dto.request.CreateStudyPlanRequest;
import com.devtrack.studyplanner.dto.response.StudyPlanResponse;
import com.devtrack.studyplanner.entity.StudyPlan;
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
class StudyPlannerServiceTest {

  @Mock private StudyPlanRepository studyPlanRepository;

  @Mock private StudyTaskRepository studyTaskRepository;

  @InjectMocks private StudyPlanService studyPlanService;

  @Test
  void createPlan_createsStudyPlan() {
    UUID userId = UUID.randomUUID();

    CreateStudyPlanRequest request =
        new CreateStudyPlanRequest("Java DSA", LocalDate.of(2026, 12, 31));

    StudyPlan plan = new StudyPlan(userId, request.title(), request.targetDate());

    when(studyPlanRepository.save(any(StudyPlan.class))).thenReturn(plan);

    when(studyTaskRepository.countByStudyPlanId(any())).thenReturn(0L);

    when(studyTaskRepository.countByStudyPlanIdAndCompletedTrue(any())).thenReturn(0L);

    StudyPlanResponse response = studyPlanService.createPlan(userId, request);

    assertEquals("Java DSA", response.title());
    assertEquals(LocalDate.of(2026, 12, 31), response.targetDate());
    assertEquals(0, response.totalTasks());
    assertEquals(0, response.completedTasks());
  }
}

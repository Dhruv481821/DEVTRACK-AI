// backend/src/test/java/com/devtrack/studyplanner/service/StudyPlanServiceTest.java  (replaces
// StudyPlannerServiceTest.java after the git mv above)
package com.devtrack.studyplanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devtrack.common.events.StudyPlanTargetDateSetEvent;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.studyplanner.dto.request.CreateStudyPlanRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyPlanRequest;
import com.devtrack.studyplanner.entity.StudyPlan;
import com.devtrack.studyplanner.repository.StudyPlanRepository;
import com.devtrack.studyplanner.repository.StudyTaskRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class StudyPlanServiceTest {

  private StudyPlanRepository studyPlanRepository;
  private StudyTaskRepository studyTaskRepository;
  private ApplicationEventPublisher eventPublisher;
  private StudyPlanService studyPlanService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    studyPlanRepository = mock(StudyPlanRepository.class);
    studyTaskRepository = mock(StudyTaskRepository.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    studyPlanService =
        new StudyPlanService(
            studyPlanRepository, studyTaskRepository, new OwnershipGuard(), eventPublisher);
    when(studyPlanRepository.save(any(StudyPlan.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void createPlan_withTargetDate_publishesTargetDateEvent() {
    CreateStudyPlanRequest request =
        new CreateStudyPlanRequest("Java DSA", LocalDate.of(2026, 12, 31));

    var response = studyPlanService.createPlan(ownerId, request);

    assertThat(response.title()).isEqualTo("Java DSA");
    assertThat(response.targetDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    verify(eventPublisher).publishEvent(any(StudyPlanTargetDateSetEvent.class));
  }

  @Test
  void createPlan_withoutTargetDate_doesNotPublishEvent() {
    CreateStudyPlanRequest request = new CreateStudyPlanRequest("Java DSA", null);

    studyPlanService.createPlan(ownerId, request);

    verifyNoInteractions(eventPublisher);
  }

  @Test
  void updatePlan_forNonOwner_throwsNotFoundAndDoesNotSave() {
    UUID planId = UUID.randomUUID();
    StudyPlan plan = new StudyPlan(ownerId, "Java DSA", null);
    when(studyPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

    assertThatThrownBy(
            () ->
                studyPlanService.updatePlan(
                    otherUserId, planId, new UpdateStudyPlanRequest("Renamed", null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(studyPlanRepository, never()).save(any());
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void updatePlan_whenPlanDoesNotExist_throwsNotFound() {
    UUID missingId = UUID.randomUUID();
    when(studyPlanRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                studyPlanService.updatePlan(
                    ownerId, missingId, new UpdateStudyPlanRequest("Renamed", null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updatePlan_settingTargetDate_publishesEvent() {
    UUID planId = UUID.randomUUID();
    StudyPlan plan = new StudyPlan(ownerId, "Java DSA", null);
    when(studyPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

    studyPlanService.updatePlan(
        ownerId, planId, new UpdateStudyPlanRequest(null, LocalDate.of(2027, 1, 1)));

    verify(eventPublisher).publishEvent(any(StudyPlanTargetDateSetEvent.class));
  }

  @Test
  void deletePlan_setsDeletedAtAndSaves() {
    UUID planId = UUID.randomUUID();
    StudyPlan plan = new StudyPlan(ownerId, "Java DSA", null);
    when(studyPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

    studyPlanService.deletePlan(ownerId, planId);

    assertThat(plan.getDeletedAt()).isNotNull();
    verify(studyPlanRepository).save(plan);
  }

  @Test
  void deletePlan_forNonOwner_throwsNotFoundAndDoesNotSave() {
    UUID planId = UUID.randomUUID();
    StudyPlan plan = new StudyPlan(ownerId, "Java DSA", null);
    when(studyPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

    assertThatThrownBy(() -> studyPlanService.deletePlan(otherUserId, planId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(studyPlanRepository, never()).save(any());
  }
}

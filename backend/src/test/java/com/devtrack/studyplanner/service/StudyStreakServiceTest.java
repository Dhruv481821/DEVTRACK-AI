// backend/src/test/java/com/devtrack/studyplanner/service/StudyStreakServiceTest.java
package com.devtrack.studyplanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.studyplanner.entity.StudyActivityLog;
import com.devtrack.studyplanner.repository.StudyActivityLogRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class StudyStreakServiceTest {

  private StudyActivityLogRepository studyActivityLogRepository;
  private StudyStreakService studyStreakService;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    studyActivityLogRepository = mock(StudyActivityLogRepository.class);
    studyStreakService = new StudyStreakService(studyActivityLogRepository);
  }

  @Test
  void recordActivity_whenNoRowForToday_savesOne() {
    when(studyActivityLogRepository.existsByUserIdAndActivityDate(userId, LocalDate.now()))
        .thenReturn(false);

    studyStreakService.recordActivity(userId);

    verify(studyActivityLogRepository).save(any(StudyActivityLog.class));
  }

  @Test
  void recordActivity_whenRowAlreadyExistsForToday_doesNotSaveAgain() {
    when(studyActivityLogRepository.existsByUserIdAndActivityDate(userId, LocalDate.now()))
        .thenReturn(true);

    studyStreakService.recordActivity(userId);

    verify(studyActivityLogRepository, never()).save(any());
  }

  @Test
  void recordActivity_toleratesConcurrentDuplicateInsert() {
    when(studyActivityLogRepository.existsByUserIdAndActivityDate(userId, LocalDate.now()))
        .thenReturn(false);
    when(studyActivityLogRepository.save(any(StudyActivityLog.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate"));

    // Should not propagate — a concurrent request already logged today's activity.
    studyStreakService.recordActivity(userId);
  }

  @Test
  void getStreak_reflectsLoggedActivityDates() {
    LocalDate today = LocalDate.now();
    when(studyActivityLogRepository.findAllByUserIdOrderByActivityDateDesc(userId))
        .thenReturn(
            List.of(
                new StudyActivityLog(userId, today),
                new StudyActivityLog(userId, today.minusDays(1))));

    var response = studyStreakService.getStreak(userId);

    assertThat(response.currentStreak()).isEqualTo(2);
    assertThat(response.longestStreak()).isEqualTo(2);
    assertThat(response.lastActivityDate()).isEqualTo(today);
  }

  @Test
  void getStreak_withNoActivity_returnsZeroesAndNullLastActivityDate() {
    when(studyActivityLogRepository.findAllByUserIdOrderByActivityDateDesc(userId))
        .thenReturn(List.of());

    var response = studyStreakService.getStreak(userId);

    assertThat(response.currentStreak()).isZero();
    assertThat(response.longestStreak()).isZero();
    assertThat(response.lastActivityDate()).isNull();
  }
}

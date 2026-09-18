package com.devtrack.studyplanner.service;

import com.devtrack.studyplanner.dto.response.StreakResponse;
import com.devtrack.studyplanner.entity.StudyActivityLog;
import com.devtrack.studyplanner.repository.StudyActivityLogRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-PLAN-02. One row per user per day (study_activity_log's unique (user_id, activity_date) index)
 * — recordActivity is a check-then-insert, with the unique constraint kept as a safety net for the
 * race between two concurrent requests on the same day, not relied on as the primary guard.
 */
@Service
@RequiredArgsConstructor
public class StudyStreakService {

  private final StudyActivityLogRepository studyActivityLogRepository;

  @Transactional
  public void recordActivity(UUID userId) {
    LocalDate today = LocalDate.now();

    if (studyActivityLogRepository.existsByUserIdAndActivityDate(userId, today)) {
      return;
    }

    try {
      studyActivityLogRepository.save(new StudyActivityLog(userId, today));
    } catch (DataIntegrityViolationException e) {
      // Another concurrent request for this user already logged today's activity first — the
      // unique index guarantees only one row can exist, so this is a benign race, not an error.
    }
  }

  @Transactional(readOnly = true)
  public StreakResponse getStreak(UUID userId) {
    List<LocalDate> datesDescending =
        studyActivityLogRepository.findAllByUserIdOrderByActivityDateDesc(userId).stream()
            .map(StudyActivityLog::getActivityDate)
            .toList();

    StreakCalculator.Result result = StreakCalculator.compute(datesDescending, LocalDate.now());

    LocalDate lastActivityDate = datesDescending.isEmpty() ? null : datesDescending.get(0);

    return new StreakResponse(result.currentStreak(), result.longestStreak(), lastActivityDate);
  }
}

package com.devtrack.studyplanner.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure computation, no I/O — see /docs/05_Database_Architecture.md §9: streaks are computed
 * in-memory from consecutive activity_date rows, not maintained as a mutable counter, so a
 * corrected or backfilled log row can never leave a stale counter behind.
 */
final class StreakCalculator {

  private StreakCalculator() {}

  record Result(int currentStreak, int longestStreak) {}

  /**
   * @param datesDescending distinct activity dates, most recent first — the same order
   *     StudyActivityLogRepository.findAllByUserIdOrderByActivityDateDesc already returns
   * @param today the reference date "today" is measured against, passed in rather than read from
   *     the clock so this stays a pure, deterministically testable function
   */
  static Result compute(List<LocalDate> datesDescending, LocalDate today) {
    if (datesDescending.isEmpty()) {
      return new Result(0, 0);
    }

    List<LocalDate> ascending = new ArrayList<>(datesDescending);
    Collections.reverse(ascending);

    int longestStreak = 1;
    int run = 1;
    for (int i = 1; i < ascending.size(); i++) {
      if (ascending.get(i).equals(ascending.get(i - 1).plusDays(1))) {
        run++;
      } else {
        run = 1;
      }
      longestStreak = Math.max(longestStreak, run);
    }

    // A streak still counts as "current" if today's activity hasn't happened yet but yesterday's
    // did — otherwise a user who hasn't logged anything yet today would see their streak reset to
    // 0 the instant midnight passes, before they've had any chance to act.
    LocalDate mostRecent = ascending.get(ascending.size() - 1);
    int currentStreak = 0;
    if (!mostRecent.isBefore(today.minusDays(1))) {
      currentStreak = 1;
      for (int i = ascending.size() - 2; i >= 0; i--) {
        if (ascending.get(i).equals(ascending.get(i + 1).minusDays(1))) {
          currentStreak++;
        } else {
          break;
        }
      }
    }

    return new Result(currentStreak, Math.max(longestStreak, currentStreak));
  }
}

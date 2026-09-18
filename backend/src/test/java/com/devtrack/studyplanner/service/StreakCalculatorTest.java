// backend/src/test/java/com/devtrack/studyplanner/service/StreakCalculatorTest.java
package com.devtrack.studyplanner.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class StreakCalculatorTest {

  private static final LocalDate TODAY = LocalDate.of(2026, 9, 18);

  @Test
  void noActivity_returnsZeroForBoth() {
    StreakCalculator.Result result = StreakCalculator.compute(List.of(), TODAY);

    assertThat(result.currentStreak()).isZero();
    assertThat(result.longestStreak()).isZero();
  }

  @Test
  void activityToday_countsAsCurrentStreakOfOne() {
    StreakCalculator.Result result = StreakCalculator.compute(List.of(TODAY), TODAY);

    assertThat(result.currentStreak()).isEqualTo(1);
    assertThat(result.longestStreak()).isEqualTo(1);
  }

  @Test
  void lastActivityYesterday_streakStillCountsAsCurrent() {
    StreakCalculator.Result result = StreakCalculator.compute(List.of(TODAY.minusDays(1)), TODAY);

    assertThat(result.currentStreak()).isEqualTo(1);
  }

  @Test
  void lastActivityTwoDaysAgo_currentStreakIsBroken() {
    StreakCalculator.Result result = StreakCalculator.compute(List.of(TODAY.minusDays(2)), TODAY);

    assertThat(result.currentStreak()).isZero();
    assertThat(result.longestStreak()).isEqualTo(1);
  }

  @Test
  void fiveConsecutiveDaysEndingToday_currentAndLongestAreFive() {
    List<LocalDate> datesDescending =
        List.of(
            TODAY, TODAY.minusDays(1), TODAY.minusDays(2), TODAY.minusDays(3), TODAY.minusDays(4));

    StreakCalculator.Result result = StreakCalculator.compute(datesDescending, TODAY);

    assertThat(result.currentStreak()).isEqualTo(5);
    assertThat(result.longestStreak()).isEqualTo(5);
  }

  @Test
  void olderLongerRunFollowedByShorterCurrentRun_longestStaysFromTheOlderRun() {
    // A 3-day run 8-10 days ago, a gap, then a 2-day current run ending today.
    List<LocalDate> datesDescending =
        List.of(
            TODAY, TODAY.minusDays(1), TODAY.minusDays(8), TODAY.minusDays(9), TODAY.minusDays(10));

    StreakCalculator.Result result = StreakCalculator.compute(datesDescending, TODAY);

    assertThat(result.currentStreak()).isEqualTo(2);
    assertThat(result.longestStreak()).isEqualTo(3);
  }
}

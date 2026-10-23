package com.devtrack.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.devtrack.analytics.dto.response.ReadinessResponse.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReadinessCalculatorTest {

  private final LocalDate today = LocalDate.of(2026, 10, 23);

  @Test
  void dsa_withNoProblems_isZeroAndAsksToLog() {
    Component c = ReadinessCalculator.dsa(new ReadinessCalculator.DsaInput(0, 0, List.of(), today));

    assertThat(c.score()).isZero();
    assertThat(c.nextStep()).contains("Log the DSA problems");
  }

  @Test
  void dsa_meetingEveryTarget_isFullScoreWithNoNextStep() {
    List<LocalDate> dates = new ArrayList<>();
    for (int i = 0; i < 12; i++) {
      dates.add(today.minusDays(i));
    }
    Component c = ReadinessCalculator.dsa(new ReadinessCalculator.DsaInput(150, 90, dates, today));

    assertThat(c.score()).isEqualTo(100);
    assertThat(c.nextStep()).isNull();
  }

  @Test
  void dsa_countsOnlyDistinctDaysInsideTheThirtyDayWindow() {
    List<LocalDate> dates =
        List.of(
            today,
            today, // same day twice counts once
            today.minusDays(10),
            today.minusDays(29), // first day of the window
            today.minusDays(30)); // outside the window
    Component c = ReadinessCalculator.dsa(new ReadinessCalculator.DsaInput(30, 6, dates, today));

    // volume 30/150*40=8, consistency 3/12*40=10, depth (0.2/0.6)*20=6.67 -> 25
    assertThat(c.score()).isEqualTo(25);
    assertThat(c.signals()).contains("Active on 3 of the last 30 days");
    assertThat(c.nextStep()).contains("volume");
  }

  @Test
  void github_notConnected_isZeroAndAsksToConnect() {
    Component c = ReadinessCalculator.github(new ReadinessCalculator.GithubInput(false, 0, 0, 0));

    assertThat(c.score()).isZero();
    assertThat(c.nextStep()).contains("Connect GitHub");
  }

  @Test
  void github_partialActivity_pointsAtTheLargestGap() {
    Component c = ReadinessCalculator.github(new ReadinessCalculator.GithubInput(true, 15, 1, 1));

    // commits 15/60*60=15, repos 1/4*25=6.25, breadth 1/3*15=5 -> 26
    assertThat(c.score()).isEqualTo(26);
    assertThat(c.nextStep()).contains("Commit more consistently");
  }

  @Test
  void github_meetingEveryTarget_isFullScore() {
    Component c = ReadinessCalculator.github(new ReadinessCalculator.GithubInput(true, 60, 4, 3));

    assertThat(c.score()).isEqualTo(100);
    assertThat(c.nextStep()).isNull();
  }

  @Test
  void resume_scoresPresentCoreSectionsAndNamesTheMissingOnes() {
    Component c =
        ReadinessCalculator.resume(
            new ReadinessCalculator.ResumeInput("Backend", Set.of("EXPERIENCE", "SKILLS")));

    assertThat(c.score()).isEqualTo(50);
    assertThat(c.nextStep()).isEqualTo("Add the missing sections: Education, Projects.");
  }

  @Test
  void resume_withoutAnyResume_isZero() {
    Component c = ReadinessCalculator.resume(new ReadinessCalculator.ResumeInput(null, Set.of()));

    assertThat(c.score()).isZero();
  }

  @Test
  void overall_isTheRoundedMeanOfComponents() {
    List<Component> components =
        List.of(
            new Component("DSA", "DSA", 100, List.of(), null),
            new Component("RESUME", "Resume", 50, List.of(), null),
            new Component("GITHUB", "GitHub", 0, List.of(), null));

    assertThat(ReadinessCalculator.overall(components)).isEqualTo(50);
  }
}

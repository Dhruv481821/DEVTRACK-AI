package com.devtrack.analytics.service;

import com.devtrack.analytics.dto.response.ReadinessResponse.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Pure scoring rules for FR-ANALYTICS-01 — no I/O, so every threshold is unit-tested directly. Each
 * component is 0-100 and built from named sub-factors with fixed weights; the overall score is
 * their unweighted mean. Thresholds are deliberately simple and documented here (and surfaced as
 * signals in the UI) so a user can see exactly why a number is what it is.
 */
public final class ReadinessCalculator {

  // DSA: volume 40 + consistency 40 + depth 20.
  static final int DSA_VOLUME_TARGET = 150; // problems for full volume credit
  static final int DSA_ACTIVE_DAYS_TARGET = 12; // active days in the last 30 (~3/week)
  static final double DSA_DEPTH_TARGET = 0.6; // share of MEDIUM+HARD for full depth credit

  // GitHub: commits 60 + active repos 25 + language breadth 15.
  static final int GH_COMMITS_TARGET = 60; // commits in the last 90 days
  static final int GH_ACTIVE_REPOS_TARGET = 4;
  static final int GH_LANGUAGES_TARGET = 3;

  // Resume: the four FR-RESUME-01 section types, 25 points each when present with content.
  static final List<String> RESUME_SECTIONS =
      List.of("EXPERIENCE", "EDUCATION", "PROJECTS", "SKILLS");

  private ReadinessCalculator() {}

  public record DsaInput(
      int totalProblems, int mediumOrHard, Collection<LocalDate> problemDates, LocalDate today) {}

  public record GithubInput(
      boolean connected, int commitsLast90Days, int activeRepos, int languageCount) {}

  public record ResumeInput(String title, Set<String> sectionTypesWithContent) {}

  public static Component dsa(DsaInput in) {
    LocalDate windowStart = in.today().minusDays(29);
    long activeDays =
        in.problemDates().stream()
            .filter(d -> !d.isBefore(windowStart) && !d.isAfter(in.today()))
            .distinct()
            .count();
    double depthShare =
        in.totalProblems() == 0 ? 0 : (double) in.mediumOrHard() / in.totalProblems();

    double volume = ratio(in.totalProblems(), DSA_VOLUME_TARGET) * 40;
    double consistency = ratio(activeDays, DSA_ACTIVE_DAYS_TARGET) * 40;
    double depth = Math.min(depthShare / DSA_DEPTH_TARGET, 1.0) * 20;
    int score = clamp(volume + consistency + depth);

    List<String> signals = new ArrayList<>();
    signals.add(in.totalProblems() + " problems logged");
    signals.add("Active on " + activeDays + " of the last 30 days");
    signals.add(Math.round(depthShare * 100) + "% medium or hard");

    String nextStep = null;
    if (score < 100) {
      double volumeGap = 40 - volume;
      double consistencyGap = 40 - consistency;
      double depthGap = 20 - depth;
      if (in.totalProblems() == 0) {
        nextStep = "Log the DSA problems you solve to start building this score.";
      } else if (consistencyGap >= volumeGap && consistencyGap >= depthGap) {
        nextStep =
            "Practice on more days: aim for "
                + DSA_ACTIVE_DAYS_TARGET
                + " active days a month (you're at "
                + activeDays
                + ").";
      } else if (depthGap >= volumeGap) {
        nextStep = "Shift toward medium and hard problems — target 60% of your practice.";
      } else {
        nextStep = "Keep building volume toward " + DSA_VOLUME_TARGET + " problems.";
      }
    }
    return new Component("DSA", "DSA practice", score, signals, nextStep);
  }

  public static Component github(GithubInput in) {
    if (!in.connected()) {
      return new Component(
          "GITHUB",
          "GitHub activity",
          0,
          List.of("GitHub not connected"),
          "Connect GitHub in Settings so your commits count toward readiness.");
    }
    double commits = ratio(in.commitsLast90Days(), GH_COMMITS_TARGET) * 60;
    double repos = ratio(in.activeRepos(), GH_ACTIVE_REPOS_TARGET) * 25;
    double breadth = ratio(in.languageCount(), GH_LANGUAGES_TARGET) * 15;
    int score = clamp(commits + repos + breadth);

    List<String> signals =
        List.of(
            in.commitsLast90Days() + " commits in the last 90 days",
            in.activeRepos() + " repositories with recent commits",
            in.languageCount() + " primary languages");

    String nextStep = null;
    if (score < 100) {
      if (60 - commits >= 25 - repos && 60 - commits >= 15 - breadth) {
        nextStep =
            "Commit more consistently: "
                + GH_COMMITS_TARGET
                + " commits per 90 days is the target (you have "
                + in.commitsLast90Days()
                + ").";
      } else if (25 - repos >= 15 - breadth) {
        nextStep = "Spread recent work across a few more repositories.";
      } else {
        nextStep = "Build something in another language to show breadth.";
      }
    }
    return new Component("GITHUB", "GitHub activity", score, signals, nextStep);
  }

  public static Component resume(ResumeInput in) {
    if (in.title() == null) {
      return new Component(
          "RESUME",
          "Resume completeness",
          0,
          List.of("No resume yet"),
          "Create a resume with Experience, Education, Projects and Skills sections.");
    }
    List<String> missing =
        RESUME_SECTIONS.stream().filter(s -> !in.sectionTypesWithContent().contains(s)).toList();
    int present = RESUME_SECTIONS.size() - missing.size();
    int score = clamp(100.0 * present / RESUME_SECTIONS.size());

    List<String> signals =
        List.of(
            "Latest resume: " + in.title(),
            present + " of " + RESUME_SECTIONS.size() + " core sections filled");
    String nextStep =
        missing.isEmpty()
            ? null
            : "Add the missing section"
                + (missing.size() > 1 ? "s: " : ": ")
                + String.join(", ", missing.stream().map(ReadinessCalculator::titleCase).toList())
                + ".";
    return new Component("RESUME", "Resume completeness", score, signals, nextStep);
  }

  /** Unweighted mean of the component scores, rounded. */
  public static int overall(List<Component> components) {
    return clamp(components.stream().mapToInt(Component::score).average().orElse(0));
  }

  private static double ratio(double value, double target) {
    return Math.max(0, Math.min(value / target, 1.0));
  }

  private static int clamp(double value) {
    return (int) Math.max(0, Math.min(100, Math.round(value)));
  }

  private static String titleCase(String upper) {
    return upper.charAt(0) + upper.substring(1).toLowerCase();
  }
}

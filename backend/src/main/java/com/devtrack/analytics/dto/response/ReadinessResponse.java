package com.devtrack.analytics.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * FR-ANALYTICS-01. A deterministic, explainable score (no AI call, no quota) — every component
 * lists the exact signals behind its number, per 09_AI_Architecture.md §9's transparency principle.
 */
public record ReadinessResponse(
    int overall, List<Component> components, List<TrendPoint> trend, LocalDate asOf) {

  /** key is DSA, RESUME or GITHUB. nextStep is null when the component is at 100. */
  public record Component(
      String key, String label, int score, List<String> signals, String nextStep) {}

  public record TrendPoint(LocalDate date, int overall, int dsa, int resume, int github) {}
}

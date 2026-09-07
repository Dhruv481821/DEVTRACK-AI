package com.devtrack.dsa.dto.response;

import java.util.Map;

/** FR-DSA-02. Computed in Java from the user's already-fetched problem list. */
public record DsaTrendsResponse(
    int totalProblems, Map<String, Long> difficultyDistribution, Map<String, Long> tagFrequency) {}

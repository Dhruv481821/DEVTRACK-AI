package com.devtrack.ai.dto.response;

import java.util.List;

/**
 * answer and recommendations come from the model (validated before use); basedOn is computed
 * server-side from which data sources actually had data — 09_AI_Architecture.md §9's sourcing
 * transparency, never a model self-report.
 */
public record CareerAssistantResult(
    String answer, List<String> recommendations, List<String> basedOn) {}

package com.devtrack.ai.dto.response;

import java.util.List;

/**
 * Mirrors AgentConfig.responseSchema's JSON Schema exactly (see ResumeAtsAgentConfig) — Jackson
 * deserializes Gemini's structured-output JSON straight into this record. A field mismatch here and
 * in that schema is exactly the "malformed provider response" case ResumeAtsService is required to
 * handle safely, not a case that should ever reach production undetected — the two are kept next to
 * each other for that reason, not out of any technical necessity to match.
 */
public record AtsScoreResult(
    int score,
    List<String> matchedKeywords,
    List<String> missingKeywords,
    List<String> formattingIssues,
    String reasoning,
    List<String> sourcesUsed) {}

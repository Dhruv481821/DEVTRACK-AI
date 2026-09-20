package com.devtrack.ai;

/**
 * Only RESUME_ATS is implemented. /docs/09_AI_Architecture.md documents five further agents across
 * later phases (Career, Interview Prep, etc.) — those are deliberately not stubbed out here as
 * placeholder enum values. Study Planner's StudyActivityLog/StreakResponse sat unused and
 * half-wired for weeks earlier in this project specifically because pre-written scaffolding for a
 * not-yet-built feature is easy to forget about; add the next value when the next agent is actually
 * built, not before.
 */
public enum AgentType {
  RESUME_ATS
}

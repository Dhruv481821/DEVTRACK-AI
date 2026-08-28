package com.devtrack.common.events;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Published by Study Planner (not built yet) whenever a study_plan's targetDate is set or changed —
 * Calendar listens (see StudyPlanEventListener) and creates/updates the corresponding event row.
 * Defined here now, before its publisher exists, so Calendar's listener side is complete and
 * waiting — the same "consumer built before producer" pattern as Phase 0's UserProfileInitializer.
 * See /docs/05_Database_Architecture.md §10.
 */
public record StudyPlanTargetDateSetEvent(
    UUID studyPlanId, UUID userId, String title, LocalDate targetDate) {}

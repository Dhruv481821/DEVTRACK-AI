package com.devtrack.studyplanner.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudyPlanResponse(
    UUID id,
    String title,
    LocalDate targetDate,
    long totalTasks,
    long completedTasks,
    Instant createdAt,
    Instant updatedAt) {}

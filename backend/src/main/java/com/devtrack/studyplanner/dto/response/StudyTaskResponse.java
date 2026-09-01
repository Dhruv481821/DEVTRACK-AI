package com.devtrack.studyplanner.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudyTaskResponse(
    UUID id,
    UUID studyPlanId,
    String title,
    boolean completed,
    LocalDate dueDate,
    Instant createdAt,
    Instant updatedAt) {}

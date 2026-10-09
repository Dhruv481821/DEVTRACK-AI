package com.devtrack.project.dto.response;

import com.devtrack.project.entity.TaskStatus;
import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID projectId,
    String title,
    TaskStatus status,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}

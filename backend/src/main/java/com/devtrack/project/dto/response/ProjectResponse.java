package com.devtrack.project.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    UUID linkedRepoSnapshotId,
    Instant createdAt,
    Instant updatedAt) {}
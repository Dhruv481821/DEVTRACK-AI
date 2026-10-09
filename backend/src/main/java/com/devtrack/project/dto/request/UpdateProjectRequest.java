package com.devtrack.project.dto.request;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateProjectRequest(
    @Size(max = 200) String name,
    @Size(max = 2000) String description,
    UUID linkedRepoSnapshotId) {}

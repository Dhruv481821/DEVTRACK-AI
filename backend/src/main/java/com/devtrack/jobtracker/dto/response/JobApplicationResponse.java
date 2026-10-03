package com.devtrack.jobtracker.dto.response;

import com.devtrack.jobtracker.entity.JobApplicationStage;
import java.time.Instant;
import java.util.UUID;

public record JobApplicationResponse(
    UUID id,
    String companyName,
    String roleTitle,
    String jobUrl,
    JobApplicationStage stage,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}

package com.devtrack.jobtracker.dto.request;

import com.devtrack.jobtracker.entity.JobApplicationStage;
import jakarta.validation.constraints.Size;

// Same nullable-partial-update pattern as UpdateCertificateRequest — a null field
// means "leave unchanged" (JobApplicationService does a null-check per field).
// stage is here so moving a card between kanban columns is just another partial
// update, not a separate endpoint.
public record UpdateJobApplicationRequest(
    @Size(max = 200) String companyName,
    @Size(max = 200) String roleTitle,
    @Size(max = 500) String jobUrl,
    String notes,
    JobApplicationStage stage) {}

package com.devtrack.jobtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Stage isn't settable on create — every new application starts at APPLIED
// (JobApplication.stage's own default), matching the kanban's leftmost column.
public record CreateJobApplicationRequest(
    @NotBlank @Size(max = 200) String companyName,
    @NotBlank @Size(max = 200) String roleTitle,
    @Size(max = 500) String jobUrl,
    String notes) {}

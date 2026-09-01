package com.devtrack.studyplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateStudyPlanRequest(
    @NotBlank @Size(max = 200, message = "Title must be 200 characters or fewer") String title,
    LocalDate targetDate) {}

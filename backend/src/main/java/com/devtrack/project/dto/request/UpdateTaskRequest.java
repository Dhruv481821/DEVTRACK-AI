package com.devtrack.project.dto.request;

import com.devtrack.project.entity.TaskStatus;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
    @Size(max = 200) String title,
    TaskStatus status,
    @Size(max = 2000) String notes) {}
package com.devtrack.dsa.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** FR-DSA-01 — logging an attempt against an existing problem. */
public record CreateDsaAttemptRequest(
    @NotNull LocalDate attemptedAt, Integer timeTakenMinutes, String notes) {}

package com.devtrack.dsa.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record DsaAttemptResponse(
    UUID id, LocalDate attemptedAt, Integer timeTakenMinutes, String notes) {}

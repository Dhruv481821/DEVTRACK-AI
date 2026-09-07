package com.devtrack.dsa.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DsaProblemResponse(
    UUID id, String title, String difficulty, List<String> tags, Instant createdAt) {}

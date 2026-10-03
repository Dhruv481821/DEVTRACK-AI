package com.devtrack.github.dto.response;

import java.time.Instant;
import java.util.UUID;

/** FR-GH-03. Mirrors RepoSnapshot's own fields exactly — no synthetic fields invented. */
public record RepoSnapshotResponse(
    UUID id,
    String repoName,
    int stars,
    String primaryLanguage,
    int commitsLast90Days,
    Instant syncedAt) {}

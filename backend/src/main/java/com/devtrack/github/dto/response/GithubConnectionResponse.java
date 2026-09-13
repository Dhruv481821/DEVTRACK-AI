package com.devtrack.github.dto.response;

import java.time.Instant;

public record GithubConnectionResponse(
        boolean connected,
        String githubUsername,
        Instant lastSyncedAt) {

    public static GithubConnectionResponse notConnected() {
        return new GithubConnectionResponse(false, null, null);
    }
}
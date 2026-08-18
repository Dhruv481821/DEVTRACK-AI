package com.devtrack.common.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Phase 0: in-memory Bucket4j, safe under the single-instance constraint
 * (04_System_Architecture.md §8). Migrates to Bucket4j's Redis-backed distributed
 * mode in Phase 2 — same library, different backend, so that migration is a
 * configuration change here, not a rewrite of every call site. See
 * /docs/07_Backend_Architecture.md §6.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** NFR-SEC-02 — strict limit on auth endpoints specifically: 5 attempts per minute per key. */
    public boolean tryConsumeAuthAttempt(String key) {
        Bucket bucket =
                buckets.computeIfAbsent(
                        "auth:" + key,
                        k -> Bucket.builder().addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))).build());
        return bucket.tryConsume(1);
    }
}

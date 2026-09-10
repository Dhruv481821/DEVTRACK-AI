package com.devtrack.common.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/**
 * Redis-backed, per the migration decided back in 07_Backend_Architecture.md §6 —
 * "same library, different backend," now due since Redis is actually standing up
 * in this phase. Replaces the Phase 0 in-memory ConcurrentHashMap version, which
 * only worked correctly under the single-instance constraint
 * (04_System_Architecture.md §8); this version is correct even if the app is ever
 * scaled to multiple instances, since the bucket state now lives in Redis, not in
 * any one instance's JVM memory.
 *
 * <p>builderFor(RedisClient) is used directly, NOT builderFor(redisClient.connect()) —
 * that connect() call returns a StatefulRedisConnection<String, String> by
 * default, which doesn't match either of LettuceBasedProxyManager's overloads
 * (it needs RedisClient directly, or a connection specifically typed
 * <K, byte[]> for Bucket4j's binary bucket-state serialization). Passing the
 * RedisClient itself lets the library manage that connection/codec internally.
 */
@Service
public class RateLimiterService {

    private final ProxyManager<byte[]> proxyManager;

    public RateLimiterService(RedisClient redisClient) {
        this.proxyManager = LettuceBasedProxyManager.builderFor(redisClient).build();
    }

    /** NFR-SEC-02 — same 5-per-minute limit as before, just backed by Redis now. */
    public boolean tryConsumeAuthAttempt(String key) {
        Supplier<BucketConfiguration> configSupplier =
                () -> BucketConfiguration.builder().addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))).build();
        Bucket bucket = proxyManager.builder().build(key.getBytes(StandardCharsets.UTF_8), configSupplier);
        return bucket.tryConsume(1);
    }
}
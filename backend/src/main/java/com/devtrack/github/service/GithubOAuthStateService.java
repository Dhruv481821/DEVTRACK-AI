package com.devtrack.github.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Maps a random OAuth state value to the userId who initiated the connect flow — needed because
 * GitHub's callback has no other way to identify which already-authenticated user requested the
 * connection (unlike a login flow, where the callback itself establishes identity). Redis is the
 * natural fit: this is short-lived, one-time-use data, exactly what a TTL-backed store is for.
 *
 * <p>Uses Spring Data Redis's auto-configured StringRedisTemplate directly — no custom bean needed
 * here, unlike RateLimiterService's raw RedisClient requirement (Bucket4j's Lettuce integration
 * specifically needs the raw client; this doesn't).
 */
@Service
public class GithubOAuthStateService {

  private static final String KEY_PREFIX = "github-oauth-state:";
  private static final Duration TTL = Duration.ofMinutes(10);

  private final StringRedisTemplate redisTemplate;

  public GithubOAuthStateService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public String createState(UUID userId) {
    String state = UUID.randomUUID().toString();
    redisTemplate.opsForValue().set(KEY_PREFIX + state, userId.toString(), TTL);
    return state;
  }

  /** Consumes the state — one-time use, deleted immediately after a successful read. */
  public Optional<UUID> consumeState(String state) {
    String key = KEY_PREFIX + state;
    String userId = redisTemplate.opsForValue().get(key);
    if (userId == null) {
      return Optional.empty();
    }
    redisTemplate.delete(key);
    return Optional.of(UUID.fromString(userId));
  }
}

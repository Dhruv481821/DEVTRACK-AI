package com.devtrack.ai.service;

import com.devtrack.ai.dto.response.AtsScoreResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Two distinct Redis-backed caches, not one — see ResumeAtsService for why: an exact-match cache
 * (TTL-expired, keyed by exactly which inputs produced a result) for the fast repeat-request path,
 * and a last-known-good pointer (never expires on its own, always overwritten on success) that
 * exists purely to satisfy /docs/09_AI_Architecture.md §8's "show the last cached response, labeled
 * with its generation date" degraded-mode requirement — a pure exact-match cache can never serve
 * that, since by construction a failing request's key has no entry.
 *
 * <p>Same StringRedisTemplate usage as GithubOAuthStateService — values are JSON-serialized since
 * Redis strings can't hold a structured object directly.
 */
@Service
public class AiResponseCacheService {

  private static final Logger log = LoggerFactory.getLogger(AiResponseCacheService.class);

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public AiResponseCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public record CachedResult(AtsScoreResult result, Instant generatedAt) {}

  public Optional<CachedResult> getExact(String key) {
    return read(key);
  }

  public void putExact(String key, AtsScoreResult result, Instant generatedAt, Duration ttl) {
    write(key, result, generatedAt, ttl);
  }

  public Optional<CachedResult> getLastKnownGood(String key) {
    return read(key);
  }

  // No TTL — a last-known-good entry is meant to survive indefinitely as a fallback; it is only
  // ever replaced by a newer successful result for the same (user, resume, job description), never
  // expired on a timer.
  public void putLastKnownGood(String key, AtsScoreResult result, Instant generatedAt) {
    write(key, result, generatedAt, null);
  }

  private Optional<CachedResult> read(String key) {
    String raw = redisTemplate.opsForValue().get(key);
    if (raw == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(objectMapper.readValue(raw, CachedResult.class));
    } catch (Exception e) {
      log.warn("Failed to deserialize cached AI response for key {}", key, e);
      return Optional.empty();
    }
  }

  private void write(String key, AtsScoreResult result, Instant generatedAt, Duration ttl) {
    try {
      String raw = objectMapper.writeValueAsString(new CachedResult(result, generatedAt));
      if (ttl != null) {
        redisTemplate.opsForValue().set(key, raw, ttl);
      } else {
        redisTemplate.opsForValue().set(key, raw);
      }
    } catch (Exception e) {
      // A cache write failure should never break the actual AI response the user is waiting on —
      // log and move on, exactly like a cache-aside pattern should behave.
      log.warn("Failed to write AI response cache for key {}", key, e);
    }
  }
}

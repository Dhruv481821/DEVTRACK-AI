package com.devtrack.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.ai.dto.response.CareerAssistantResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/** Round-trips the agent-agnostic cache API through real Jackson (generic record + Instant). */
class AiResponseCacheServiceGenericTest {

  @Test
  @SuppressWarnings("unchecked")
  void putThenGet_roundTripsTypedResult() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    ValueOperations<String, String> ops = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(ops);
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    AiResponseCacheService cache = new AiResponseCacheService(redisTemplate, objectMapper);

    CareerAssistantResult result =
        new CareerAssistantResult("answer", List.of("do x"), List.of("GitHub (2 repositories)"));
    Instant generatedAt = Instant.parse("2026-10-23T10:15:30Z");

    cache.put("k", result, generatedAt, Duration.ofHours(6));

    ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
    verify(ops).set(eq("k"), stored.capture(), eq(Duration.ofHours(6)));
    when(ops.get(anyString())).thenReturn(stored.getValue());

    var read = cache.get("k", CareerAssistantResult.class).orElseThrow();

    assertThat(read.result()).isEqualTo(result);
    assertThat(read.generatedAt()).isEqualTo(generatedAt);
  }
}

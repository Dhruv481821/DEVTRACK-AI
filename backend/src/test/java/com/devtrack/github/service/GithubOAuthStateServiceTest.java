package com.devtrack.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class GithubOAuthStateServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private GithubOAuthStateService stateService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        stateService = new GithubOAuthStateService(redisTemplate);
    }

    @Test
    void createState_storesUserIdWithATtl() {
        UUID userId = UUID.randomUUID();

        String state = stateService.createState(userId);

        assertThat(state).isNotBlank();
        verify(valueOperations)
                .set(
                        "github-oauth-state:" + state,
                        userId.toString(),
                        Duration.ofMinutes(10));
    }

    @Test
    void consumeState_withValidState_returnsUserIdAndDeletesIt() {
        UUID userId = UUID.randomUUID();
        String state = "some-state-value";

        when(valueOperations.get("github-oauth-state:" + state))
                .thenReturn(userId.toString());

        Optional<UUID> result = stateService.consumeState(state);

        assertThat(result).contains(userId);

        verify(redisTemplate)
                .delete("github-oauth-state:" + state);
    }

    /**
     * The scenario that matters most for this class — an expired or already-used
     * state must not silently succeed.
     */
    @Test
    void consumeState_withUnknownOrExpiredState_returnsEmptyAndDoesNotDelete() {
        when(valueOperations.get(anyString())).thenReturn(null);

        Optional<UUID> result =
                stateService.consumeState("never-issued-or-expired-state");

        assertThat(result).isEmpty();

        verify(redisTemplate, never())
                .delete(any(String.class));
    }
}
package com.devtrack.github.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
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
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        stateService = new GithubOAuthStateService(redisTemplate);
    }

    @Test
    void createState_returnsStateAndStoresUserId() {
        UUID userId = UUID.randomUUID();

        String state = stateService.createState(userId);

        assertThat(state).isNotBlank();

        verify(valueOperations)
                .set(
                        eq("github-oauth-state:" + state),
                        eq(userId.toString()),
                        eq(Duration.ofMinutes(10)));
    }

    @Test
    void consumeState_returnsUserIdAndDeletesState() {
        UUID userId = UUID.randomUUID();
        String state = UUID.randomUUID().toString();

        when(valueOperations.get("github-oauth-state:" + state))
                .thenReturn(userId.toString());

        assertThat(stateService.consumeState(state))
                .contains(userId);

        verify(redisTemplate)
                .delete("github-oauth-state:" + state);
    }

    @Test
    void consumeState_unknownState_returnsEmpty() {
        String state = UUID.randomUUID().toString();

        when(valueOperations.get("github-oauth-state:" + state))
                .thenReturn(null);

        assertThat(stateService.consumeState(state))
                .isEmpty();
    }

    @Test
    void consumeState_twice_secondAttemptReturnsEmpty() {
        UUID userId = UUID.randomUUID();
        String state = UUID.randomUUID().toString();

        when(valueOperations.get("github-oauth-state:" + state))
                .thenReturn(userId.toString())
                .thenReturn(null);

        assertThat(stateService.consumeState(state))
                .contains(userId);

        assertThat(stateService.consumeState(state))
                .isEmpty();

        verify(redisTemplate)
                .delete("github-oauth-state:" + state);
    }
}
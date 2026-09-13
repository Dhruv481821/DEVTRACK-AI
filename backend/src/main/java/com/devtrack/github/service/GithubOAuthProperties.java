package com.devtrack.github.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds devtrack.github-oauth.* — mirrors the pattern of JwtProperties/EmailProperties.
 */
@ConfigurationProperties(prefix = "devtrack.github-oauth")
public record GithubOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri) {}
package com.devtrack.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds devtrack.cors.allowed-origin — strict allowlist, never a wildcard. See 12_Security.md §11. */
@ConfigurationProperties(prefix = "devtrack.cors")
public record CorsProperties(String allowedOrigin) {}

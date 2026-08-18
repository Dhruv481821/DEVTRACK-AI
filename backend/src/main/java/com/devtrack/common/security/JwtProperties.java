package com.devtrack.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds devtrack.jwt.* — see application-dev.yml / application-prod.yml. */
@ConfigurationProperties(prefix = "devtrack.jwt")
public record JwtProperties(String privateKey, String publicKey) {}

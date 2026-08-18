package com.devtrack.common.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds devtrack.resend.api-key and devtrack.frontend.base-url (for building links in emails). */
@ConfigurationProperties(prefix = "devtrack")
public record EmailProperties(Resend resend, Frontend frontend) {
  public record Resend(String apiKey) {}

  public record Frontend(String baseUrl) {}
}

package com.devtrack.common.email;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Real Resend API integration (not a stub) — per your "never generate placeholder implementations"
 * rule. Untested against a live Resend account in this environment (no network access to
 * api.resend.com from this sandbox) — the request shape below matches Resend's documented /emails
 * endpoint, but verify against a real API key before trusting it in production. See
 * /docs/15_Deployment.md §1 step 5.
 */
@Service
@EnableConfigurationProperties(EmailProperties.class)
public class ResendEmailService implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
  private static final String FROM_ADDRESS =
      "DevTrack AI <noreply@devtrack.ai>"; // update once a domain is verified — 15_Deployment.md §1
  // step 5

  private final RestClient restClient;

  public ResendEmailService(EmailProperties properties) {
    this.restClient =
        RestClient.builder()
            .baseUrl("https://api.resend.com")
            .defaultHeader("Authorization", "Bearer " + properties.resend().apiKey())
            .build();
  }

  @Override
  public void sendVerificationEmail(String toEmail, String verificationLink) {
    send(
        toEmail,
        "Verify your DevTrack AI account",
        "<p>Click to verify your email:</p><p><a href=\""
            + verificationLink
            + "\">Verify email</a></p>");
  }

  @Override
  public void sendPasswordResetEmail(String toEmail, String resetLink) {
    send(
        toEmail,
        "Reset your DevTrack AI password",
        "<p>Click to reset your password (expires in 30 minutes):</p><p><a href=\""
            + resetLink
            + "\">Reset password</a></p>");
  }

  private void send(String toEmail, String subject, String htmlBody) {
    try {
      restClient
          .post()
          .uri("/emails")
          .body(Map.of("from", FROM_ADDRESS, "to", toEmail, "subject", subject, "html", htmlBody))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      // Email delivery failure should never surface as a 500 to the user mid-registration —
      // log it for investigation, but the account/token still exists and can be
      // resent/retried. Swallowing here is a deliberate choice, not an oversight.
      log.error("Failed to send email via Resend to {}", toEmail, e);
    }
  }
}

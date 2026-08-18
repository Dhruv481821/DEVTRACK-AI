package com.devtrack.common.email;

/**
 * Interface exists specifically so the provider is swappable (07_Backend_Architecture.md §1) — one
 * implementation for v1 (Resend), but no call site anywhere in the codebase depends on that
 * directly.
 */
public interface EmailService {

  void sendVerificationEmail(String toEmail, String verificationLink);

  void sendPasswordResetEmail(String toEmail, String resetLink);
}

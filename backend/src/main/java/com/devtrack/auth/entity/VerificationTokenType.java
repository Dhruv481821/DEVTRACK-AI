package com.devtrack.auth.entity;

/**
 * Which flow a verification_token row belongs to — FR-AUTH-05 and email verification share one
 * table.
 */
public enum VerificationTokenType {
  PASSWORD_RESET,
  EMAIL_VERIFICATION
}

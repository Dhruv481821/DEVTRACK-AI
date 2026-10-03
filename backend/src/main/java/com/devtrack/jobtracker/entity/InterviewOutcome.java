package com.devtrack.jobtracker.entity;

/**
 * FR-INT-01. Minimal outcome set — not modeling WITHDRAWN/ON_HOLD/etc. until a real need shows up.
 */
public enum InterviewOutcome {
  PENDING,
  PASSED,
  FAILED
}

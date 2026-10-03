package com.devtrack.jobtracker.entity;

/** FR-JOB-01's kanban stages, per PRD §10/§11: Applied → Screening → Interview → Offer/Rejected. */
public enum JobApplicationStage {
  APPLIED,
  SCREENING,
  INTERVIEW,
  OFFER,
  REJECTED
}

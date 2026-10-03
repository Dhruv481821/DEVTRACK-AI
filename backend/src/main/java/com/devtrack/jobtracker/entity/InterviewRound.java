package com.devtrack.jobtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * FR-INT-01. @ManyToOne to JobApplication is intra-module — same pattern as ResumeSection ->
 * Resume. occurredAt isn't named in the FR's own wording ("log interview rounds ... with notes and
 * outcomes") but is included anyway: an interview round is inherently a dated event, the same
 * reasoning DsaAttempt.attemptDate already applies to logged DSA attempts in this codebase — not
 * new scope, the same minimal-logged-event shape applied here.
 *
 * <p>No soft delete, no delete endpoint — same "not part of the currently planned surface" call
 * ResumeSectionService's docblock already makes for resume sections.
 */
@Entity
@Table(name = "interview_round")
@Getter
@Setter
@NoArgsConstructor
public class InterviewRound {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_application_id", nullable = false)
  private JobApplication jobApplication;

  @Column(name = "round_name", nullable = false)
  private String roundName;

  @Column(name = "occurred_at")
  private Instant occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private InterviewOutcome outcome = InterviewOutcome.PENDING;

  @Column private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof InterviewRound other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

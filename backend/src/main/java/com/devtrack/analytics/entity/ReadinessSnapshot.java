package com.devtrack.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** FR-ANALYTICS-01. Written only via ReadinessSnapshotRepository.upsert (idempotent per day). */
@Entity
@Table(name = "readiness_snapshot")
@Getter
@Setter
@NoArgsConstructor
public class ReadinessSnapshot {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "snapshot_date", nullable = false)
  private LocalDate snapshotDate;

  @Column(name = "overall_score", nullable = false)
  private int overallScore;

  @Column(name = "dsa_score", nullable = false)
  private int dsaScore;

  @Column(name = "resume_score", nullable = false)
  private int resumeScore;

  @Column(name = "github_score", nullable = false)
  private int githubScore;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ReadinessSnapshot other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

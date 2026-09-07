package com.devtrack.dsa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * @ManyToOne to DsaProblem is intra-module — same pattern as StudyTask -> StudyPlan.
 */
@Entity
@Table(name = "dsa_attempt")
@Getter
@Setter
@NoArgsConstructor
public class DsaAttempt {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dsa_problem_id", nullable = false)
  private DsaProblem dsaProblem;

  @Column(name = "attempted_at", nullable = false)
  private LocalDate attemptedAt;

  @Column(name = "time_taken_minutes")
  private Integer timeTakenMinutes;

  @Column private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DsaAttempt other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

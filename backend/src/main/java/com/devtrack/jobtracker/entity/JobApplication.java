package com.devtrack.jobtracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

/**
 * FR-JOB-01. user_id is a scalar — same cross-module boundary rule as every other module.
 * Soft-deletable, same pattern as Resume. @SQLRestriction enforces soft delete.
 */
@Entity
@Table(name = "job_application")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class JobApplication {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "company_name", nullable = false)
  private String companyName;

  @Column(name = "role_title", nullable = false)
  private String roleTitle;

  @Column(name = "job_url")
  private String jobUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private JobApplicationStage stage = JobApplicationStage.APPLIED;

  @Column private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof JobApplication other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

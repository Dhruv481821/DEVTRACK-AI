package com.devtrack.studyplanner.entity;

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
 * @ManyToOne to StudyPlan IS a real JPA relationship, unlike user_id elsewhere — StudyTask and
 * StudyPlan live in the same module, so this is an intra-module relationship, not a cross-module
 * one (07_Backend_Architecture.md §4's boundary rule only applies across modules). No user_id
 * column here — ownership flows through studyPlan.userId; no deleted_at — v1 has no task-delete
 * endpoint (06_API_Specification.md §4.3).
 */
@Entity
@Table(name = "study_task")
@Getter
@Setter
@NoArgsConstructor
public class StudyTask {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_plan_id", nullable = false)
  private StudyPlan studyPlan;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private boolean completed = false;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StudyTask other)) return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}

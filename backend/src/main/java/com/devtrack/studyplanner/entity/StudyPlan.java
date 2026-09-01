package com.devtrack.studyplanner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "study_plan")
@SQLRestriction("deleted_at IS NULL")
public class StudyPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String title;

  @Column(name = "target_date")
  private LocalDate targetDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(mappedBy = "studyPlan", fetch = FetchType.LAZY)
  private List<StudyTask> tasks = new ArrayList<>();

  protected StudyPlan() {}

  public StudyPlan(UUID userId, String title, LocalDate targetDate) {
    this.userId = userId;
    this.title = title;
    this.targetDate = targetDate;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public LocalDate getTargetDate() {
    return targetDate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public List<StudyTask> getTasks() {
    return tasks;
  }

  public void update(String title, LocalDate targetDate) {
    if (title != null) {
      this.title = title;
    }
    this.targetDate = targetDate;
    this.updatedAt = Instant.now();
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
    this.updatedAt = this.deletedAt;
  }
}

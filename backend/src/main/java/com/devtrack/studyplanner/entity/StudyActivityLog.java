package com.devtrack.studyplanner.entity;

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

@Entity
@Table(name = "study_activity_log")
@Getter
@Setter
@NoArgsConstructor
public class StudyActivityLog {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "activity_date", nullable = false)
  private LocalDate activityDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public StudyActivityLog(UUID userId, LocalDate activityDate) {
    this.userId = userId;
    this.activityDate = activityDate;
    this.createdAt = Instant.now();
  }
}

package com.devtrack.analytics.repository;

import com.devtrack.analytics.entity.ReadinessSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadinessSnapshotRepository extends JpaRepository<ReadinessSnapshot, UUID> {

  List<ReadinessSnapshot> findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(
      UUID userId, LocalDate from);

  /**
   * One statement, atomic under concurrency: two requests computing readiness on the same day both
   * succeed and leave exactly one row (the unique (user_id, snapshot_date) index is the target).
   */
  @Modifying
  @Query(
      value =
          """
          insert into readiness_snapshot
              (user_id, snapshot_date, overall_score, dsa_score, resume_score, github_score)
          values (:userId, :date, :overall, :dsa, :resume, :github)
          on conflict (user_id, snapshot_date) do update set
              overall_score = excluded.overall_score,
              dsa_score = excluded.dsa_score,
              resume_score = excluded.resume_score,
              github_score = excluded.github_score,
              updated_at = now()
          """,
      nativeQuery = true)
  void upsert(
      @Param("userId") UUID userId,
      @Param("date") LocalDate date,
      @Param("overall") int overall,
      @Param("dsa") int dsa,
      @Param("resume") int resume,
      @Param("github") int github);
}

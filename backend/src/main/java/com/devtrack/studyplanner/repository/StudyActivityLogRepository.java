package com.devtrack.studyplanner.repository;

import com.devtrack.studyplanner.entity.StudyActivityLog;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyActivityLogRepository extends JpaRepository<StudyActivityLog, UUID> {

  boolean existsByUserIdAndActivityDate(UUID userId, LocalDate activityDate);

  List<StudyActivityLog> findAllByUserIdOrderByActivityDateDesc(UUID userId);
}

package com.devtrack.studyplanner.repository;

import com.devtrack.studyplanner.entity.StudyPlan;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {

  List<StudyPlan> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<StudyPlan> findByIdAndUserId(UUID id, UUID userId);
}

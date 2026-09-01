package com.devtrack.studyplanner.repository;

import com.devtrack.studyplanner.entity.StudyTask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTaskRepository extends JpaRepository<StudyTask, UUID> {

  List<StudyTask> findAllByStudyPlanIdOrderByCreatedAtAsc(UUID studyPlanId);

  Optional<StudyTask> findByIdAndStudyPlanId(UUID id, UUID studyPlanId);

  long countByStudyPlanId(UUID studyPlanId);

  long countByStudyPlanIdAndCompletedTrue(UUID studyPlanId);
}

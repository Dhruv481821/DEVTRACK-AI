package com.devtrack.jobtracker.repository;

import com.devtrack.jobtracker.entity.InterviewRound;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRoundRepository extends JpaRepository<InterviewRound, UUID> {

  List<InterviewRound> findByJobApplicationIdOrderByOccurredAtAsc(UUID jobApplicationId);

  // Scoped by BOTH roundId and jobApplicationId — same ID-confusion protection as
  // ResumeSectionRepository.findByIdAndResumeId.
  Optional<InterviewRound> findByIdAndJobApplicationId(UUID roundId, UUID jobApplicationId);
}

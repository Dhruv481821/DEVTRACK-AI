package com.devtrack.resume.repository;

import com.devtrack.resume.entity.ResumeSection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeSectionRepository extends JpaRepository<ResumeSection, UUID> {

  List<ResumeSection> findByResumeIdOrderByOrderIndexAsc(UUID resumeId);

  // Scoped by BOTH sectionId and resumeId — same ID-confusion protection as
  // StudyTaskRepository.findByIdAndStudyPlanId.
  Optional<ResumeSection> findByIdAndResumeId(UUID sectionId, UUID resumeId);
}

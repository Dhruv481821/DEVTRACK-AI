package com.devtrack.jobtracker.repository;

import com.devtrack.jobtracker.entity.JobApplication;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

  List<JobApplication> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}

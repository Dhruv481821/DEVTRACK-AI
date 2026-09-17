package com.devtrack.resume.repository;

import com.devtrack.resume.entity.Resume;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {

  List<Resume> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}

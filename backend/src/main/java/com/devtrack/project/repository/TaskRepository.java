package com.devtrack.project.repository;

import com.devtrack.project.entity.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {

  List<Task> findByProjectIdOrderByCreatedAtDesc(UUID projectId);

  Optional<Task> findByIdAndProjectId(UUID id, UUID projectId);
}

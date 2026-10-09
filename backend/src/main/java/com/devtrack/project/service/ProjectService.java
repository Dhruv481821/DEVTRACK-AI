package com.devtrack.project.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.project.dto.request.CreateProjectRequest;
import com.devtrack.project.dto.request.UpdateProjectRequest;
import com.devtrack.project.dto.response.ProjectResponse;
import com.devtrack.project.entity.Project;
import com.devtrack.project.repository.ProjectRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

  private final ProjectRepository projectRepository;

  public ProjectService(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Transactional(readOnly = true)
  public List<ProjectResponse> getProjects(UUID userId) {
    return projectRepository.findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
    Instant now = Instant.now();

    Project project = new Project();
    project.setUserId(userId);
    project.setName(request.name().trim());
    project.setDescription(request.description());
    project.setLinkedRepoSnapshotId(request.linkedRepoSnapshotId());
    project.setCreatedAt(now);
    project.setUpdatedAt(now);

    return toResponse(projectRepository.save(project));
  }

  public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
    Project project = getOwnedProject(userId, projectId);

    if (request.name() != null) {
      project.setName(request.name().trim());
    }
    if (request.description() != null) {
      project.setDescription(request.description());
    }
    if (request.linkedRepoSnapshotId() != null) {
      project.setLinkedRepoSnapshotId(request.linkedRepoSnapshotId());
    }

    project.setUpdatedAt(Instant.now());
    return toResponse(projectRepository.save(project));
  }

  public void deleteProject(UUID userId, UUID projectId) {
    Project project = getOwnedProject(userId, projectId);
    project.setDeletedAt(Instant.now());
    project.setUpdatedAt(Instant.now());
    projectRepository.save(project);
  }

  private Project getOwnedProject(UUID userId, UUID projectId) {
    return projectRepository
        .findByIdAndUserIdAndDeletedAtIsNull(projectId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private ProjectResponse toResponse(Project project) {
    return new ProjectResponse(
        project.getId(),
        project.getName(),
        project.getDescription(),
        project.getLinkedRepoSnapshotId(),
        project.getCreatedAt(),
        project.getUpdatedAt());
  }
}

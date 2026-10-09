
package com.devtrack.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.project.dto.request.CreateProjectRequest;
import com.devtrack.project.dto.request.UpdateProjectRequest;
import com.devtrack.project.entity.Project;
import com.devtrack.project.repository.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectServiceTest {

  private ProjectRepository projectRepository;
  private ProjectService projectService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    projectRepository = mock(ProjectRepository.class);
    projectService = new ProjectService(projectRepository);

    when(projectRepository.save(any(Project.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void createProject_storesProjectDetails() {
    UUID repoId = UUID.randomUUID();

    var response = projectService.createProject(
        ownerId, new CreateProjectRequest("DevTrack", "description", repoId));

    assertThat(response.name()).isEqualTo("DevTrack");
    assertThat(response.description()).isEqualTo("description");
    assertThat(response.linkedRepoSnapshotId()).isEqualTo(repoId);
    verify(projectRepository).save(any(Project.class));
  }

  @Test
  void updateProject_changesProvidedFields() {
    Project project = existingProject(ownerId);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), ownerId)).thenReturn(Optional.of(project));

    var response = projectService.updateProject(
        ownerId, project.getId(),
        new UpdateProjectRequest("Renamed", null, null));

    assertThat(response.name()).isEqualTo("Renamed");
    assertThat(response.description()).isEqualTo("original description");
  }

  @Test
  void updateProject_forNonOwner_throwsNotFound() {
    Project project = existingProject(ownerId);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> projectService.updateProject(
        otherUserId, project.getId(),
        new UpdateProjectRequest("Renamed", null, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(projectRepository, never()).save(any(Project.class));
  }

  @Test
  void deleteProject_setsDeletedAt() {
    Project project = existingProject(ownerId);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), ownerId)).thenReturn(Optional.of(project));

    projectService.deleteProject(ownerId, project.getId());

    assertThat(project.getDeletedAt()).isNotNull();
    verify(projectRepository).save(project);
  }

  @Test
  void deleteProject_forNonOwner_throwsNotFound() {
    Project project = existingProject(ownerId);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> projectService.deleteProject(otherUserId, project.getId()))
        .isInstanceOf(ResourceNotFoundException.class);

    assertThat(project.getDeletedAt()).isNull();
    verify(projectRepository, never()).save(any(Project.class));
  }

  private Project existingProject(UUID userId) {
    Project project = new Project();
    project.setId(UUID.randomUUID());
    project.setUserId(userId);
    project.setName("A project");
    project.setDescription("original description");
    return project;
  }
}

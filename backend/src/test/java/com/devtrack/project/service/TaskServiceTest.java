
package com.devtrack.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.project.dto.request.CreateTaskRequest;
import com.devtrack.project.dto.request.UpdateTaskRequest;
import com.devtrack.project.entity.Project;
import com.devtrack.project.entity.Task;
import com.devtrack.project.entity.TaskStatus;
import com.devtrack.project.repository.ProjectRepository;
import com.devtrack.project.repository.TaskRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

  private ProjectRepository projectRepository;
  private TaskRepository taskRepository;
  private TaskService taskService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    projectRepository = mock(ProjectRepository.class);
    taskRepository = mock(TaskRepository.class);
    taskService = new TaskService(projectRepository, taskRepository);

    when(taskRepository.save(any(Task.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void getTasks_forNonOwner_throwsNotFound() {
    UUID projectId = UUID.randomUUID();
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        projectId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getTasks(otherUserId, projectId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createTask_defaultsToTodo() {
    Project project = existingProject(ownerId);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), ownerId)).thenReturn(Optional.of(project));

    var response = taskService.createTask(
        ownerId, project.getId(), new CreateTaskRequest("Write tests", null));

    assertThat(response.title()).isEqualTo("Write tests");
    assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    assertThat(response.projectId()).isEqualTo(project.getId());
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void createTask_forNonOwner_throwsNotFoundAndDoesNotSave() {
    UUID projectId = UUID.randomUUID();
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        projectId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.createTask(
        otherUserId, projectId, new CreateTaskRequest("Write tests", null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(taskRepository, never()).save(any(Task.class));
  }

  @Test
  void updateTask_changesProvidedFields() {
    Project project = existingProject(ownerId);
    Task task = existingTask(project);
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        project.getId(), ownerId)).thenReturn(Optional.of(project));
    when(taskRepository.findByIdAndProjectId(
        task.getId(), project.getId())).thenReturn(Optional.of(task));

    var response = taskService.updateTask(
        ownerId, project.getId(), task.getId(),
        new UpdateTaskRequest("Updated title", TaskStatus.DONE, null));

    assertThat(response.title()).isEqualTo("Updated title");
    assertThat(response.status()).isEqualTo(TaskStatus.DONE);
  }

  @Test
  void updateTask_forNonOwner_throwsNotFound() {
    UUID projectId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();
    when(projectRepository.findByIdAndUserIdAndDeletedAtIsNull(
        projectId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.updateTask(
        otherUserId, projectId, taskId,
        new UpdateTaskRequest("Updated title", TaskStatus.DONE, null)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(taskRepository, never()).save(any(Task.class));
  }

  private Project existingProject(UUID userId) {
    Project project = new Project();
    project.setId(UUID.randomUUID());
    project.setUserId(userId);
    project.setName("A project");
    project.setDescription("description");
    return project;
  }

  private Task existingTask(Project project) {
    Task task = new Task();
    task.setId(UUID.randomUUID());
    task.setProject(project);
    task.setTitle("Original task");
    task.setStatus(TaskStatus.TODO);
    return task;
  }
}

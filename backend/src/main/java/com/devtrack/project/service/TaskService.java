package com.devtrack.project.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.project.dto.request.CreateTaskRequest;
import com.devtrack.project.dto.request.UpdateTaskRequest;
import com.devtrack.project.dto.response.TaskResponse;
import com.devtrack.project.entity.Project;
import com.devtrack.project.entity.Task;
import com.devtrack.project.entity.TaskStatus;
import com.devtrack.project.repository.ProjectRepository;
import com.devtrack.project.repository.TaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;

  public TaskService(ProjectRepository projectRepository, TaskRepository taskRepository) {
    this.projectRepository = projectRepository;
    this.taskRepository = taskRepository;
  }

  @Transactional(readOnly = true)
  public List<TaskResponse> getTasks(UUID userId, UUID projectId) {
    getOwnedProject(userId, projectId);

    return taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
        .map(this::toResponse)
        .toList();
  }

  public TaskResponse createTask(UUID userId, UUID projectId, CreateTaskRequest request) {
    Project project = getOwnedProject(userId, projectId);
    Instant now = Instant.now();

    Task task = new Task();
    task.setProject(project);
    task.setTitle(request.title().trim());
    task.setNotes(request.notes());
    task.setStatus(TaskStatus.TODO);
    task.setCreatedAt(now);
    task.setUpdatedAt(now);

    return toResponse(taskRepository.save(task));
  }

  public TaskResponse updateTask(
      UUID userId, UUID projectId, UUID taskId, UpdateTaskRequest request) {
    getOwnedProject(userId, projectId);

    Task task =
        taskRepository
            .findByIdAndProjectId(taskId, projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

    if (request.title() != null) {
      task.setTitle(request.title().trim());
    }
    if (request.status() != null) {
      task.setStatus(request.status());
    }
    if (request.notes() != null) {
      task.setNotes(request.notes());
    }

    task.setUpdatedAt(Instant.now());
    return toResponse(taskRepository.save(task));
  }

  private Project getOwnedProject(UUID userId, UUID projectId) {
    return projectRepository
        .findByIdAndUserIdAndDeletedAtIsNull(projectId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
  }

  private TaskResponse toResponse(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getProject().getId(),
        task.getTitle(),
        task.getStatus(),
        task.getNotes(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}

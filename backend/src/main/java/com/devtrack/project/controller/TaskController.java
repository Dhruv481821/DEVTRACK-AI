package com.devtrack.project.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.project.dto.request.CreateTaskRequest;
import com.devtrack.project.dto.request.UpdateTaskRequest;
import com.devtrack.project.dto.response.TaskResponse;
import com.devtrack.project.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {

  private final TaskService taskService;
  private final CurrentUserResolver currentUserResolver;

  public TaskController(
      TaskService taskService, CurrentUserResolver currentUserResolver) {
    this.taskService = taskService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<TaskResponse>> list(@PathVariable UUID projectId) {
    return ApiEnvelope.success(
        taskService.getTasks(currentUserResolver.getCurrentUserId(), projectId));
  }

  @PostMapping
  public ApiEnvelope<TaskResponse> create(
      @PathVariable UUID projectId, @Valid @RequestBody CreateTaskRequest request) {
    return ApiEnvelope.success(
        taskService.createTask(
            currentUserResolver.getCurrentUserId(), projectId, request));
  }

  @PatchMapping("/{taskId}")
  public ApiEnvelope<TaskResponse> update(
      @PathVariable UUID projectId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateTaskRequest request) {
    return ApiEnvelope.success(
        taskService.updateTask(
            currentUserResolver.getCurrentUserId(), projectId, taskId, request));
  }
}
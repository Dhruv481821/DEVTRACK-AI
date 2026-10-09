package com.devtrack.project.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.project.dto.request.CreateProjectRequest;
import com.devtrack.project.dto.request.UpdateProjectRequest;
import com.devtrack.project.dto.response.ProjectResponse;
import com.devtrack.project.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final CurrentUserResolver currentUserResolver;

  public ProjectController(
      ProjectService projectService, CurrentUserResolver currentUserResolver) {
    this.projectService = projectService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<ProjectResponse>> list() {
    return ApiEnvelope.success(
        projectService.getProjects(currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<ProjectResponse> create(
      @Valid @RequestBody CreateProjectRequest request) {
    return ApiEnvelope.success(
        projectService.createProject(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<ProjectResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateProjectRequest request) {
    return ApiEnvelope.success(
        projectService.updateProject(currentUserResolver.getCurrentUserId(), id, request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    projectService.deleteProject(currentUserResolver.getCurrentUserId(), id);
    return ApiEnvelope.success(null);
  }
}
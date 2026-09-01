package com.devtrack.studyplanner.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.studyplanner.dto.request.CreateStudyTaskRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyTaskRequest;
import com.devtrack.studyplanner.dto.response.StudyTaskResponse;
import com.devtrack.studyplanner.service.StudyTaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study-plans/{planId}/tasks")
public class StudyTaskController {

  private final StudyTaskService studyTaskService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<ApiEnvelope<StudyTaskResponse>> createTask(
      @PathVariable UUID planId, @Valid @RequestBody CreateStudyTaskRequest request) {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.status(201)
        .body(ApiEnvelope.success(studyTaskService.createTask(userId, planId, request)));
  }

  @GetMapping
  public ResponseEntity<ApiEnvelope<List<StudyTaskResponse>>> getTasks(@PathVariable UUID planId) {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.ok(ApiEnvelope.success(studyTaskService.getTasks(userId, planId)));
  }

  @PatchMapping("/{taskId}")
  public ResponseEntity<ApiEnvelope<StudyTaskResponse>> updateTask(
      @PathVariable UUID planId,
      @PathVariable UUID taskId,
      @Valid @RequestBody UpdateStudyTaskRequest request) {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.ok(
        ApiEnvelope.success(studyTaskService.updateTask(userId, planId, taskId, request)));
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> deleteTask(@PathVariable UUID planId, @PathVariable UUID taskId) {

    UUID userId = currentUserResolver.getCurrentUserId();

    studyTaskService.deleteTask(userId, planId, taskId);

    return ResponseEntity.noContent().build();
  }
}

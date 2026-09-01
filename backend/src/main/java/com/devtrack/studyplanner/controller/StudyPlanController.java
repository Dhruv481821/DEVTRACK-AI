package com.devtrack.studyplanner.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.studyplanner.dto.request.CreateStudyPlanRequest;
import com.devtrack.studyplanner.dto.request.UpdateStudyPlanRequest;
import com.devtrack.studyplanner.dto.response.StudyPlanResponse;
import com.devtrack.studyplanner.service.StudyPlanService;
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
@RequestMapping("/api/v1/study-plans")
public class StudyPlanController {

  private final StudyPlanService studyPlanService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<ApiEnvelope<StudyPlanResponse>> createPlan(
      @Valid @RequestBody CreateStudyPlanRequest request) {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.status(201)
        .body(ApiEnvelope.success(studyPlanService.createPlan(userId, request)));
  }

  @GetMapping
  public ResponseEntity<ApiEnvelope<List<StudyPlanResponse>>> getPlans() {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.ok(ApiEnvelope.success(studyPlanService.getPlans(userId)));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiEnvelope<StudyPlanResponse>> updatePlan(
      @PathVariable UUID id, @Valid @RequestBody UpdateStudyPlanRequest request) {

    UUID userId = currentUserResolver.getCurrentUserId();

    return ResponseEntity.ok(ApiEnvelope.success(studyPlanService.updatePlan(userId, id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePlan(@PathVariable UUID id) {

    UUID userId = currentUserResolver.getCurrentUserId();

    studyPlanService.deletePlan(userId, id);

    return ResponseEntity.noContent().build();
  }
}

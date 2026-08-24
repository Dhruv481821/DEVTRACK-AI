package com.devtrack.notes.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.notes.dto.response.TagResponse;
import com.devtrack.notes.repository.TagRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Supports a tag picker/autocomplete UI — per /docs/06_API_Specification.md §4.1. */
@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

  private final TagRepository tagRepository;
  private final CurrentUserResolver currentUserResolver;

  public TagController(TagRepository tagRepository, CurrentUserResolver currentUserResolver) {
    this.tagRepository = tagRepository;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<TagResponse>> list() {
    List<TagResponse> tags =
        tagRepository.findByUserId(currentUserResolver.getCurrentUserId()).stream()
            .map(t -> new TagResponse(t.getId(), t.getName()))
            .toList();
    return ApiEnvelope.success(tags);
  }
}

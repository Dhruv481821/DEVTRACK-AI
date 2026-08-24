package com.devtrack.notes.controller;

import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import com.devtrack.notes.dto.request.CreateNoteRequest;
import com.devtrack.notes.dto.request.UpdateNoteRequest;
import com.devtrack.notes.dto.response.NoteResponse;
import com.devtrack.notes.service.NoteService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-NOTES-01, per /docs/06_API_Specification.md §4.1. */
@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

  private final NoteService noteService;
  private final CurrentUserResolver currentUserResolver;

  public NoteController(NoteService noteService, CurrentUserResolver currentUserResolver) {
    this.noteService = noteService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<NoteResponse>> list(Pageable pageable) {
    return ApiEnvelope.paginated(
        noteService.listMyNotes(currentUserResolver.getCurrentUserId(), pageable));
  }

  @GetMapping("/{id}")
  public ApiEnvelope<NoteResponse> get(@PathVariable UUID id) {
    return ApiEnvelope.success(noteService.getMyNote(id, currentUserResolver.getCurrentUserId()));
  }

  @PostMapping
  public ApiEnvelope<NoteResponse> create(@Valid @RequestBody CreateNoteRequest request) {
    return ApiEnvelope.success(
        noteService.createNote(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<NoteResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateNoteRequest request) {
    return ApiEnvelope.success(
        noteService.updateNote(id, currentUserResolver.getCurrentUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    noteService.deleteNote(id, currentUserResolver.getCurrentUserId());
    return ApiEnvelope.success(null);
  }
}

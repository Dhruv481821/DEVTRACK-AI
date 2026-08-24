package com.devtrack.notes.service;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.notes.dto.request.CreateNoteRequest;
import com.devtrack.notes.dto.request.UpdateNoteRequest;
import com.devtrack.notes.dto.response.NoteResponse;
import com.devtrack.notes.entity.Note;
import com.devtrack.notes.entity.Tag;
import com.devtrack.notes.repository.NoteRepository;
import com.devtrack.notes.repository.TagRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-NOTES-01. Search (FR-NOTES-02) is deliberately not implemented in this slice — per
 * /docs/05_Database_Architecture.md §4, this project committed to Postgres full-text search
 * (tsvector + GIN), not a LIKE-based query, for anything called "search." Building that properly
 * needs its own migration (a generated tsvector column + index) and is a distinct enough piece of
 * work to be its own slice rather than bolted onto basic CRUD here.
 */
@Service
public class NoteService {

  private final NoteRepository noteRepository;
  private final TagRepository tagRepository;
  private final OwnershipGuard ownershipGuard;

  public NoteService(
      NoteRepository noteRepository, TagRepository tagRepository, OwnershipGuard ownershipGuard) {
    this.noteRepository = noteRepository;
    this.tagRepository = tagRepository;
    this.ownershipGuard = ownershipGuard;
  }

  @Transactional(readOnly = true)
  public Page<NoteResponse> listMyNotes(UUID userId, Pageable pageable) {
    return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable).map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public NoteResponse getMyNote(UUID noteId, UUID userId) {
    Note note = findOrThrow(noteId);
    ownershipGuard.assertOwnedBy(note.getUserId(), userId);
    return toResponse(note);
  }

  @Transactional
  public NoteResponse createNote(UUID userId, CreateNoteRequest request) {
    Note note = new Note();
    note.setUserId(userId);
    note.setTitle(request.title());
    note.setContent(request.content() != null ? request.content() : Map.of());
    note.setTags(resolveTags(userId, request.tagNames()));
    note.setCreatedAt(Instant.now());
    note.setUpdatedAt(Instant.now());
    noteRepository.save(note);
    return toResponse(note);
  }

  @Transactional
  public NoteResponse updateNote(UUID noteId, UUID userId, UpdateNoteRequest request) {
    Note note = findOrThrow(noteId);
    ownershipGuard.assertOwnedBy(note.getUserId(), userId);

    if (request.title() != null) {
      note.setTitle(request.title());
    }
    if (request.content() != null) {
      note.setContent(request.content());
    }
    if (request.tagNames() != null) {
      note.setTags(resolveTags(userId, request.tagNames()));
    }
    note.setUpdatedAt(Instant.now());
    noteRepository.save(note);
    return toResponse(note);
  }

  /**
   * Soft delete (NFR-REL-01) — Note's @SQLRestriction means this row simply stops appearing in any
   * query from here on.
   */
  @Transactional
  public void deleteNote(UUID noteId, UUID userId) {
    Note note = findOrThrow(noteId);
    ownershipGuard.assertOwnedBy(note.getUserId(), userId);
    note.setDeletedAt(Instant.now());
    noteRepository.save(note);
  }

  private Note findOrThrow(UUID noteId) {
    return noteRepository
        .findById(noteId)
        .orElseThrow(() -> new ResourceNotFoundException("Note not found."));
  }

  /**
   * Find-or-create per user — a tag name that doesn't exist yet for this user is created
   * transparently.
   */
  private Set<Tag> resolveTags(UUID userId, List<String> tagNames) {
    if (tagNames == null) {
      return new HashSet<>();
    }
    Set<Tag> tags = new HashSet<>();
    for (String name : tagNames) {
      Tag tag =
          tagRepository
              .findByUserIdAndName(userId, name)
              .orElseGet(
                  () -> {
                    Tag newTag = new Tag();
                    newTag.setUserId(userId);
                    newTag.setName(name);
                    return tagRepository.save(newTag);
                  });
      tags.add(tag);
    }
    return tags;
  }

  private NoteResponse toResponse(Note note) {
    List<String> tagNames = note.getTags().stream().map(Tag::getName).sorted().toList();
    return new NoteResponse(
        note.getId(),
        note.getTitle(),
        note.getContent(),
        tagNames,
        note.getCreatedAt(),
        note.getUpdatedAt());
  }
}

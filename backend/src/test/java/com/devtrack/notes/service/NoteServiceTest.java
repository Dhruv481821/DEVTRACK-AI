package com.devtrack.notes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.notes.dto.request.CreateNoteRequest;
import com.devtrack.notes.dto.request.UpdateNoteRequest;
import com.devtrack.notes.dto.response.NoteResponse;
import com.devtrack.notes.entity.Note;
import com.devtrack.notes.entity.Tag;
import com.devtrack.notes.repository.NoteRepository;
import com.devtrack.notes.repository.TagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests (repositories mocked, per /docs/13_Testing.md §2's "unit (service layer)" row). The
 * real OwnershipGuard is used directly (not mocked) — it's a tiny, dependency-free class, and
 * testing NoteService against the real guard is more meaningful than testing against a stub that
 * could silently drift from what the guard actually does.
 */
class NoteServiceTest {

  private NoteRepository noteRepository;
  private TagRepository tagRepository;
  private NoteService noteService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    noteRepository = mock(NoteRepository.class);
    tagRepository = mock(TagRepository.class);
    noteService = new NoteService(noteRepository, tagRepository, new OwnershipGuard());
  }

  @Test
  void createNote_resolvesExistingAndNewTagsCorrectly() {
    Tag existingTag = new Tag();
    existingTag.setId(UUID.randomUUID());
    existingTag.setUserId(ownerId);
    existingTag.setName("existing");
    when(tagRepository.findByUserIdAndName(ownerId, "existing"))
        .thenReturn(Optional.of(existingTag));
    when(tagRepository.findByUserIdAndName(ownerId, "brand-new")).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenAnswer(inv -> inv.getArgument(0));
    when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

    NoteResponse response =
        noteService.createNote(
            ownerId,
            new CreateNoteRequest(
                "Title", Map.of("type", "doc"), List.of("existing", "brand-new")));

    assertThat(response.tags()).containsExactlyInAnyOrder("existing", "brand-new");
    verify(tagRepository)
        .save(any(Tag.class)); // only the new one triggers a save — the existing one doesn't
  }

  @Test
  void getMyNote_forOwner_returnsNote() {
    Note note = existingNote(ownerId);
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

    NoteResponse response = noteService.getMyNote(note.getId(), ownerId);

    assertThat(response.id()).isEqualTo(note.getId());
  }

  /**
   * The one that actually matters — mirrors 13_Testing.md §4's "one shared test suite against
   * assertOwnership()" guarantee, exercised here through a real caller rather than testing
   * OwnershipGuard in isolation a second time.
   */
  @Test
  void getMyNote_forNonOwner_throwsNotFoundNotForbidden() {
    Note note = existingNote(ownerId);
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

    assertThatThrownBy(() -> noteService.getMyNote(note.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateNote_onlyOverwritesProvidedFields() {
    Note note = existingNote(ownerId);
    note.setTitle("Original");
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
    when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

    // Only title provided — content/tags must be left untouched.
    noteService.updateNote(note.getId(), ownerId, new UpdateNoteRequest("New Title", null, null));

    assertThat(note.getTitle()).isEqualTo("New Title");
    assertThat(note.getContent()).isEqualTo(Map.of("original", true));
  }

  @Test
  void updateNote_forNonOwner_throwsNotFound() {
    Note note = existingNote(ownerId);
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

    assertThatThrownBy(
            () ->
                noteService.updateNote(
                    note.getId(), otherUserId, new UpdateNoteRequest("x", null, null)))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(noteRepository, never()).save(any());
  }

  @Test
  void deleteNote_setsDeletedAtInsteadOfHardDeleting() {
    Note note = existingNote(ownerId);
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));
    when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

    noteService.deleteNote(note.getId(), ownerId);

    assertThat(note.getDeletedAt())
        .isNotNull(); // NFR-REL-01 — soft delete, never a repository.delete() call
    verify(noteRepository, never()).deleteById(any());
  }

  @Test
  void deleteNote_forNonOwner_throwsNotFoundAndDoesNotDelete() {
    Note note = existingNote(ownerId);
    when(noteRepository.findById(note.getId())).thenReturn(Optional.of(note));

    assertThatThrownBy(() -> noteService.deleteNote(note.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
    assertThat(note.getDeletedAt()).isNull();
  }

  private Note existingNote(UUID ownerId) {
    Note note = new Note();
    note.setId(UUID.randomUUID());
    note.setUserId(ownerId);
    note.setTitle("A note");
    note.setContent(Map.of("original", true));
    return note;
  }
}

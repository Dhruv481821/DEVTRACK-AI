package com.devtrack.resume.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.security.OwnershipGuard;
import com.devtrack.resume.dto.request.CreateResumeRequest;
import com.devtrack.resume.dto.request.UpdateResumeRequest;
import com.devtrack.resume.entity.Resume;
import com.devtrack.resume.repository.ResumeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResumeServiceTest {
  private ResumeRepository resumeRepository;
  private ResumeService resumeService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    resumeRepository = mock(ResumeRepository.class);
    resumeService = new ResumeService(resumeRepository, new OwnershipGuard());

    when(resumeRepository.save(any(Resume.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void listMyResumes_mapsRepositoryResultsInOrder() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findByUserIdOrderByUpdatedAtDesc(ownerId)).thenReturn(List.of(resume));

    List<com.devtrack.resume.dto.response.ResumeResponse> responses =
        resumeService.listMyResumes(ownerId);

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).id()).isEqualTo(resume.getId());
    assertThat(responses.get(0).title()).isEqualTo(resume.getTitle());
  }

  @Test
  void getMyResume_forNonOwner_throwsNotFound() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));

    assertThatThrownBy(() -> resumeService.getMyResume(resume.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getMyResume_whenIdDoesNotExist_throwsNotFound() {
    UUID missingId = UUID.randomUUID();

    when(resumeRepository.findById(missingId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> resumeService.getMyResume(missingId, ownerId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void createResume_persistsWithGivenTitleAndOwner() {
    var response =
        resumeService.createResume(ownerId, new CreateResumeRequest("Backend Engineer Resume"));

    assertThat(response.title()).isEqualTo("Backend Engineer Resume");
    verify(resumeRepository).save(any(Resume.class));
  }

  @Test
  void updateResume_withNewTitle_updatesAndSaves() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));

    var response =
        resumeService.updateResume(resume.getId(), ownerId, new UpdateResumeRequest("Renamed"));

    assertThat(response.title()).isEqualTo("Renamed");
  }

  @Test
  void updateResume_forNonOwner_throwsNotFoundAndDoesNotSave() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));

    assertThatThrownBy(
            () ->
                resumeService.updateResume(
                    resume.getId(), otherUserId, new UpdateResumeRequest("x")))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(resumeRepository, never()).save(any());
  }

  @Test
  void deleteResume_setsDeletedAtAndSaves() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));

    resumeService.deleteResume(resume.getId(), ownerId);

    assertThat(resume.getDeletedAt()).isNotNull();
    verify(resumeRepository).save(resume);
  }

  @Test
  void deleteResume_forNonOwner_throwsNotFoundAndDoesNotSave() {
    Resume resume = existingResume(ownerId);

    when(resumeRepository.findById(resume.getId())).thenReturn(Optional.of(resume));

    assertThatThrownBy(() -> resumeService.deleteResume(resume.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(resumeRepository, never()).save(any());
  }

  private Resume existingResume(UUID userId) {
    Resume resume = new Resume();
    resume.setId(UUID.randomUUID());
    resume.setUserId(userId);
    resume.setTitle("A resume");
    resume.setCreatedAt(Instant.now());
    resume.setUpdatedAt(Instant.now());
    return resume;
  }
}

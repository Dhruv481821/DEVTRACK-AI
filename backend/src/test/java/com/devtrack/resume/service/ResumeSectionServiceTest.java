package com.devtrack.resume.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.resume.dto.request.CreateResumeSectionRequest;
import com.devtrack.resume.dto.request.UpdateResumeSectionRequest;
import com.devtrack.resume.entity.Resume;
import com.devtrack.resume.entity.ResumeSection;
import com.devtrack.resume.entity.SectionType;
import com.devtrack.resume.repository.ResumeSectionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResumeSectionServiceTest {
  private ResumeSectionRepository resumeSectionRepository;
  private ResumeService resumeService;
  private ResumeSectionService resumeSectionService;

  private final UUID ownerId = UUID.randomUUID();
  private final UUID otherUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    resumeSectionRepository = mock(ResumeSectionRepository.class);
    resumeService = mock(ResumeService.class);
    resumeSectionService = new ResumeSectionService(resumeSectionRepository, resumeService);

    when(resumeSectionRepository.save(any(ResumeSection.class)))
        .thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void listSectionsForResume_forNonOwner_throwsNotFound() {
    Resume resume = existingResume(ownerId);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doThrow(new ResourceNotFoundException("Resume not found."))
        .when(resumeService)
        .assertOwned(resume, otherUserId);

    assertThatThrownBy(
            () -> resumeSectionService.listSectionsForResume(resume.getId(), otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void listSectionsForResume_returnsSectionsInRepositoryOrder() {
    Resume resume = existingResume(ownerId);
    ResumeSection section = existingSection(resume, SectionType.EXPERIENCE, 0);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doNothing().when(resumeService).assertOwned(resume, ownerId);

    when(resumeSectionRepository.findByResumeIdOrderByOrderIndexAsc(resume.getId()))
        .thenReturn(List.of(section));

    var responses = resumeSectionService.listSectionsForResume(resume.getId(), ownerId);

    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).sectionType()).isEqualTo("EXPERIENCE");
  }

  @Test
  void createSection_forNonOwner_throwsNotFoundAndDoesNotSave() {
    Resume resume = existingResume(ownerId);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doThrow(new ResourceNotFoundException("Resume not found."))
        .when(resumeService)
        .assertOwned(resume, otherUserId);

    assertThatThrownBy(
            () ->
                resumeSectionService.createSection(
                    resume.getId(),
                    otherUserId,
                    new CreateResumeSectionRequest(SectionType.SKILLS, Map.of(), 0)))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(resumeSectionRepository, never()).save(any());
  }

  @Test
  void createSection_persistsWithGivenTypeAndContent() {
    Resume resume = existingResume(ownerId);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doNothing().when(resumeService).assertOwned(resume, ownerId);

    var response =
        resumeSectionService.createSection(
            resume.getId(),
            ownerId,
            new CreateResumeSectionRequest(SectionType.PROJECTS, Map.of("title", "DevTrack"), 2));

    assertThat(response.sectionType()).isEqualTo("PROJECTS");
    assertThat(response.content()).containsEntry("title", "DevTrack");
    assertThat(response.orderIndex()).isEqualTo(2);
  }

  @Test
  void createSection_withNullContent_defaultsToEmptyMap() {
    Resume resume = existingResume(ownerId);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doNothing().when(resumeService).assertOwned(resume, ownerId);

    var response =
        resumeSectionService.createSection(
            resume.getId(), ownerId, new CreateResumeSectionRequest(SectionType.SKILLS, null, 0));

    assertThat(response.content()).isEmpty();
  }

  @Test
  void updateSection_updatesContentAndOrderIndexButNotType() {
    Resume resume = existingResume(ownerId);
    ResumeSection section = existingSection(resume, SectionType.EDUCATION, 1);

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doNothing().when(resumeService).assertOwned(resume, ownerId);

    when(resumeSectionRepository.findByIdAndResumeId(section.getId(), resume.getId()))
        .thenReturn(Optional.of(section));

    var response =
        resumeSectionService.updateSection(
            resume.getId(),
            section.getId(),
            ownerId,
            new UpdateResumeSectionRequest(Map.of("school", "MIT"), 5));

    assertThat(response.content()).containsEntry("school", "MIT");
    assertThat(response.orderIndex()).isEqualTo(5);
    assertThat(response.sectionType()).isEqualTo("EDUCATION");
  }

  @Test
  void updateSection_whenSectionDoesNotBelongToGivenResume_throwsNotFound() {
    Resume resume = existingResume(ownerId);
    UUID mismatchedSectionId = UUID.randomUUID();

    when(resumeService.findOrThrow(resume.getId())).thenReturn(resume);

    doNothing().when(resumeService).assertOwned(resume, ownerId);

    when(resumeSectionRepository.findByIdAndResumeId(mismatchedSectionId, resume.getId()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                resumeSectionService.updateSection(
                    resume.getId(),
                    mismatchedSectionId,
                    ownerId,
                    new UpdateResumeSectionRequest(null, null)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  private Resume existingResume(UUID userId) {
    Resume resume = new Resume();
    resume.setId(UUID.randomUUID());
    resume.setUserId(userId);
    resume.setTitle("A resume");
    return resume;
  }

  private ResumeSection existingSection(Resume resume, SectionType type, int orderIndex) {
    ResumeSection section = new ResumeSection();
    section.setId(UUID.randomUUID());
    section.setResume(resume);
    section.setSectionType(type);
    section.setContent(Map.of());
    section.setOrderIndex(orderIndex);
    return section;
  }
}

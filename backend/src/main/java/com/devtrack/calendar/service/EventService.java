package com.devtrack.calendar.service;

import com.devtrack.calendar.dto.request.CreateEventRequest;
import com.devtrack.calendar.dto.request.UpdateEventRequest;
import com.devtrack.calendar.dto.response.EventResponse;
import com.devtrack.calendar.entity.Event;
import com.devtrack.calendar.entity.EventSource;
import com.devtrack.calendar.repository.EventRepository;
import com.devtrack.common.exception.ResourceNotFoundException;
import com.devtrack.common.exception.ValidationException;
import com.devtrack.common.security.OwnershipGuard;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-CAL-01. Per /docs/06_API_Specification.md §4.2: there is no endpoint to create a non-MANUAL
 * event directly — those only ever come from {@link #upsertFromSource}, called by event-bus
 * listeners (StudyPlanEventListener today, a future Job Tracker listener in Phase 3). A client
 * attempting to edit an auto-generated event gets a deliberate, clear rejection, not silent success
 * that would desync it from its source.
 */
@Service
public class EventService {

  private final EventRepository eventRepository;
  private final OwnershipGuard ownershipGuard;

  public EventService(EventRepository eventRepository, OwnershipGuard ownershipGuard) {
    this.eventRepository = eventRepository;
    this.ownershipGuard = ownershipGuard;
  }

  @Transactional(readOnly = true)
  public List<EventResponse> listMyEvents(UUID userId, Instant from, Instant to) {
    return eventRepository.findByUserIdAndStartAtBetweenOrderByStartAtAsc(userId, from, to).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public EventResponse createEvent(UUID userId, CreateEventRequest request) {
    Event event = new Event();
    event.setUserId(userId);
    event.setTitle(request.title());
    event.setDescription(request.description());
    event.setStartAt(request.startAt());
    event.setEndAt(request.endAt());
    event.setSource(EventSource.MANUAL);
    event.setCreatedAt(Instant.now());
    event.setUpdatedAt(Instant.now());
    eventRepository.save(event);
    return toResponse(event);
  }

  @Transactional
  public EventResponse updateEvent(UUID eventId, UUID userId, UpdateEventRequest request) {
    Event event = findOrThrow(eventId);
    ownershipGuard.assertOwnedBy(event.getUserId(), userId);
    assertManual(event);

    if (request.title() != null) {
      event.setTitle(request.title());
    }
    if (request.description() != null) {
      event.setDescription(request.description());
    }
    if (request.startAt() != null) {
      event.setStartAt(request.startAt());
    }
    if (request.endAt() != null) {
      event.setEndAt(request.endAt());
    }
    event.setUpdatedAt(Instant.now());
    eventRepository.save(event);
    return toResponse(event);
  }

  @Transactional
  public void deleteEvent(UUID eventId, UUID userId) {
    Event event = findOrThrow(eventId);
    ownershipGuard.assertOwnedBy(event.getUserId(), userId);
    assertManual(event);
    event.setDeletedAt(Instant.now());
    eventRepository.save(event);
  }

  /**
   * Called by event-bus listeners only — never by a controller. Upserts by (source, sourceId): a
   * repeat event for the same source row (e.g. a study plan's target date changing) updates the
   * existing row instead of creating a duplicate, per /docs/05_Database_Architecture.md §10.
   */
  @Transactional
  public void upsertFromSource(
      UUID userId, EventSource source, UUID sourceId, String title, Instant startAt) {
    Event event = eventRepository.findBySourceAndSourceId(source, sourceId).orElseGet(Event::new);
    boolean isNew = event.getId() == null;

    event.setUserId(userId);
    event.setTitle(title);
    event.setStartAt(startAt);
    event.setSource(source);
    event.setSourceId(sourceId);
    event.setUpdatedAt(Instant.now());
    if (isNew) {
      event.setCreatedAt(Instant.now());
    }
    eventRepository.save(event);
  }

  private void assertManual(Event event) {
    if (event.getSource() != EventSource.MANUAL) {
      throw new ValidationException(
          "This event is managed automatically and can't be edited directly.");
    }
  }

  private Event findOrThrow(UUID eventId) {
    return eventRepository
        .findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
  }

  private EventResponse toResponse(Event event) {
    return new EventResponse(
        event.getId(),
        event.getTitle(),
        event.getDescription(),
        event.getStartAt(),
        event.getEndAt(),
        event.getSource().name());
  }
}

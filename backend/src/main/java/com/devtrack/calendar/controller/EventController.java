package com.devtrack.calendar.controller;

import com.devtrack.calendar.dto.request.CreateEventRequest;
import com.devtrack.calendar.dto.request.UpdateEventRequest;
import com.devtrack.calendar.dto.response.EventResponse;
import com.devtrack.calendar.service.EventService;
import com.devtrack.common.dto.ApiEnvelope;
import com.devtrack.common.security.CurrentUserResolver;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** FR-CAL-01, per /docs/06_API_Specification.md §4.2. */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

  private final EventService eventService;
  private final CurrentUserResolver currentUserResolver;

  public EventController(EventService eventService, CurrentUserResolver currentUserResolver) {
    this.eventService = eventService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public ApiEnvelope<List<EventResponse>> list(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return ApiEnvelope.success(
        eventService.listMyEvents(currentUserResolver.getCurrentUserId(), from, to));
  }

  @PostMapping
  public ApiEnvelope<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
    return ApiEnvelope.success(
        eventService.createEvent(currentUserResolver.getCurrentUserId(), request));
  }

  @PatchMapping("/{id}")
  public ApiEnvelope<EventResponse> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateEventRequest request) {
    return ApiEnvelope.success(
        eventService.updateEvent(id, currentUserResolver.getCurrentUserId(), request));
  }

  @DeleteMapping("/{id}")
  public ApiEnvelope<Void> delete(@PathVariable UUID id) {
    eventService.deleteEvent(id, currentUserResolver.getCurrentUserId());
    return ApiEnvelope.success(null);
  }
}

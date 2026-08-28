package com.devtrack.calendar.service;

import com.devtrack.calendar.entity.EventSource;
import com.devtrack.common.events.StudyPlanTargetDateSetEvent;
import java.time.ZoneOffset;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Dormant until Study Planner (next module) actually publishes StudyPlanTargetDateSetEvent — this
 * is the "consumer built before producer" pattern from Phase 0's UserProfileInitializer, applied
 * here. Calendar has zero knowledge of Study Planner's existence beyond this one event type, per
 * 04_System_Architecture.md §3.5's module-decoupling design.
 */
@Component
public class StudyPlanEventListener {

  private final EventService eventService;

  public StudyPlanEventListener(EventService eventService) {
    this.eventService = eventService;
  }

  @EventListener
  public void onStudyPlanTargetDateSet(StudyPlanTargetDateSetEvent event) {
    eventService.upsertFromSource(
        event.userId(),
        EventSource.STUDY_PLAN,
        event.studyPlanId(),
        event.title(),
        event.targetDate().atStartOfDay(ZoneOffset.UTC).toInstant());
  }
}

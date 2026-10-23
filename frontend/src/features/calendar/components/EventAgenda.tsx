import { useMemo, useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { useDeleteEvent, useEvents, type EventResponse } from '../api/useEvents';
import { localDayKey, monthRange } from '../lib/eventTime';

const timeFormat: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: '2-digit' };

function formatTime(event: EventResponse): string {
  const start = new Date(event.startAt);
  // Midnight start with no end = an all-day style entry (date-only input).
  if (!event.endAt && start.getHours() === 0 && start.getMinutes() === 0) {
    return 'All day';
  }
  const startText = start.toLocaleTimeString(undefined, timeFormat);
  return event.endAt
    ? `${startText} – ${new Date(event.endAt).toLocaleTimeString(undefined, timeFormat)}`
    : startText;
}

export function EventAgenda() {
  const now = new Date();
  const [cursor, setCursor] = useState({ year: now.getFullYear(), month: now.getMonth() });
  const { from, to } = monthRange(cursor.year, cursor.month);

  const { data: events, isLoading, isError } = useEvents(from, to);
  const deleteMutation = useDeleteEvent();
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  const days = useMemo(() => {
    const groups = new Map<string, EventResponse[]>();
    const sorted = [...(events ?? [])].sort((a, b) => a.startAt.localeCompare(b.startAt));
    for (const event of sorted) {
      const key = localDayKey(event.startAt);
      groups.set(key, [...(groups.get(key) ?? []), event]);
    }
    return [...groups.entries()];
  }, [events]);

  const monthLabel = new Date(cursor.year, cursor.month, 1).toLocaleDateString(undefined, {
    month: 'long',
    year: 'numeric',
  });

  const shift = (delta: number) =>
    setCursor(({ year, month }) => {
      const d = new Date(year, month + delta, 1);
      return { year: d.getFullYear(), month: d.getMonth() };
    });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <h2 className="font-display text-lg">{monthLabel}</h2>
        <div className="flex gap-2">
          <Button
            type="button"
            className="bg-surface-raised text-text-muted"
            aria-label="Previous month"
            onClick={() => shift(-1)}
          >
            ←
          </Button>
          <Button
            type="button"
            className="bg-surface-raised text-text-muted"
            onClick={() => setCursor({ year: now.getFullYear(), month: now.getMonth() })}
          >
            Today
          </Button>
          <Button
            type="button"
            className="bg-surface-raised text-text-muted"
            aria-label="Next month"
            onClick={() => shift(1)}
          >
            →
          </Button>
        </div>
      </div>

      {isLoading && <p className="font-body text-sm text-text-muted">Loading events…</p>}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load events. Please try again.
        </p>
      )}

      {!isLoading && !isError && days.length === 0 && (
        <p className="font-body text-sm text-text-muted">No events this month.</p>
      )}

      {days.map(([day, dayEvents]) => (
        <section key={day} aria-label={day} className="space-y-2">
          <h3 className="font-body text-sm font-medium text-text-muted">
            {new Date(`${day}T00:00`).toLocaleDateString(undefined, {
              weekday: 'short',
              month: 'short',
              day: 'numeric',
            })}
          </h3>

          {dayEvents.map((event) => {
            const manual = event.source === 'MANUAL';
            return (
              <Card key={event.id} className="flex items-start justify-between gap-4 p-4">
                <div className="min-w-0">
                  <p className="font-body text-sm font-medium text-text-primary">
                    {event.title}
                  </p>
                  <p className="font-mono text-xs text-text-muted">{formatTime(event)}</p>
                  {event.description && (
                    <p className="mt-1 font-body text-sm text-text-muted">
                      {event.description}
                    </p>
                  )}
                  {!manual && (
                    <p className="mt-1 font-body text-xs text-signal">
                      From study plan
                    </p>
                  )}
                </div>

                {manual &&
                  (confirmingId === event.id ? (
                    <div className="flex shrink-0 items-center gap-2">
                      <Button
                        type="button"
                        className="bg-danger"
                        loading={deleteMutation.isPending}
                        onClick={() =>
                          deleteMutation.mutate(event.id, {
                            onSettled: () => setConfirmingId(null),
                          })
                        }
                      >
                        Confirm
                      </Button>
                      <Button
                        type="button"
                        className="bg-surface-raised text-text-muted"
                        onClick={() => setConfirmingId(null)}
                      >
                        Cancel
                      </Button>
                    </div>
                  ) : (
                    <button
                      type="button"
                      onClick={() => setConfirmingId(event.id)}
                      className="shrink-0 font-body text-sm text-text-muted hover:text-danger"
                    >
                      Delete
                    </button>
                  ))}
              </Card>
            );
          })}
        </section>
      ))}

      {deleteMutation.isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t delete that event. Please try again.
        </p>
      )}
    </div>
  );
}

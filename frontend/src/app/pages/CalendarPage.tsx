import { Card } from '@/shared/ui/Card';
import { CreateEventForm } from '@/features/calendar/components/CreateEventForm';
import { EventAgenda } from '@/features/calendar/components/EventAgenda';

export function CalendarPage() {
  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl">Calendar</h1>
      <Card>
        <CreateEventForm />
      </Card>
      <EventAgenda />
    </div>
  );
}

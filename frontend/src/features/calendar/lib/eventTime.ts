// Calendar events are Instants on the backend. The UI works in the viewer's
// local time zone: form inputs (date + optional time) are local, and the
// agenda groups events by local calendar day.

// Local date ("YYYY-MM-DD") + optional local time ("HH:mm") -> ISO instant.
export function localToIso(date: string, time?: string): string {
  return new Date(`${date}T${time && time.length > 0 ? time : '00:00'}`).toISOString();
}

// [start of month, start of next month) in local time, as ISO instants — the
// window passed to GET /api/v1/events?from=&to=.
export function monthRange(year: number, month: number): { from: string; to: string } {
  return {
    from: new Date(year, month, 1).toISOString(),
    to: new Date(year, month + 1, 1).toISOString(),
  };
}

// Local "YYYY-MM-DD" key for grouping.
export function localDayKey(iso: string): string {
  const d = new Date(iso);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mm}-${dd}`;
}

import { describe, it, expect } from 'vitest';
import { localDayKey, localToIso, monthRange } from '../eventTime';

describe('eventTime', () => {
  it('treats a missing time as local midnight', () => {
    expect(localToIso('2026-03-15')).toBe(new Date(2026, 2, 15, 0, 0).toISOString());
  });

  it('combines date and time in local time', () => {
    expect(localToIso('2026-03-15', '09:30')).toBe(
      new Date(2026, 2, 15, 9, 30).toISOString(),
    );
  });

  it('builds a half-open month window that rolls over the year', () => {
    const { from, to } = monthRange(2026, 11);
    expect(from).toBe(new Date(2026, 11, 1).toISOString());
    expect(to).toBe(new Date(2027, 0, 1).toISOString());
  });

  it('groups by local calendar day', () => {
    expect(localDayKey(new Date(2026, 0, 5, 23, 59).toISOString())).toBe('2026-01-05');
  });
});

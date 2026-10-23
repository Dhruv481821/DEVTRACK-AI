import { describe, it, expect } from 'vitest';
import { formatRetryAfter, quotaMessage } from '../formatRetryAfter';

describe('formatRetryAfter', () => {
  it('formats hours and minutes', () => {
    expect(formatRetryAfter(3 * 3600 + 25 * 60)).toBe('3h 25m');
  });

  it('formats minutes only under an hour', () => {
    expect(formatRetryAfter(600)).toBe('10m');
  });

  it('falls back to the bare message without a valid header', () => {
    expect(quotaMessage('Limit reached.', null)).toBe('Limit reached.');
    expect(quotaMessage('Limit reached.', 'abc')).toBe('Limit reached.');
    expect(quotaMessage('Limit reached.', '3600')).toBe('Limit reached. Resets in 1h 0m.');
  });
});

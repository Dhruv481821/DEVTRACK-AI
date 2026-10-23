import { describe, it, expect } from 'vitest';
import { formatInr } from '../razorpayCheckout';

describe('formatInr', () => {
  it('formats paise as whole rupees with Indian grouping', () => {
    expect(formatInr(19_900)).toBe('₹199');
    expect(formatInr(199_900)).toBe('₹1,999');
    expect(formatInr(10_000_000)).toBe('₹1,00,000');
  });
});

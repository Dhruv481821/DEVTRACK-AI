import { describe, it, expect, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from '../App';

// Razorpay's KYC review opens these pages logged out — they must render without auth.
const PUBLIC_PAGES: Array<[string, RegExp]> = [
  ['/pricing', /^pricing$/i],
  ['/terms', /terms of service/i],
  ['/privacy', /privacy policy/i],
  ['/refunds', /refund & cancellation policy/i],
  ['/contact', /^contact$/i],
];

describe('public legal pages', () => {
  afterEach(() => cleanup());

  it.each(PUBLIC_PAGES)('%s renders without logging in', async (path, heading) => {
    window.history.pushState({}, '', path);
    render(
      <QueryClientProvider client={new QueryClient()}>
        <App />
      </QueryClientProvider>,
    );
    expect(await screen.findByRole('heading', { level: 1, name: heading })).toBeInTheDocument();
    expect(screen.queryByText(/log in to devtrack ai/i)).not.toBeInTheDocument();
  });
});

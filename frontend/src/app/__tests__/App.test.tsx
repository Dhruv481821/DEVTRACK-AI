import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from '../App';

// Updated for Week 3's real routing — App now redirects "/" to "/login" by
// default (unauthenticated), so that's the actual smoke-test assertion now.
// Real critical-path tests (login submit, refresh flow, per 13_Testing.md §4)
// are the next slice, not this file.
describe('App', () => {
  it('redirects to the login page when unauthenticated', () => {
    const queryClient = new QueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>,
    );
    expect(screen.getByText(/log in to devtrack ai/i)).toBeInTheDocument();
  });
});

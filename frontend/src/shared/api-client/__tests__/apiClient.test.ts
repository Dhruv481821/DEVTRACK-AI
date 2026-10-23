import { describe, it, expect, vi, afterEach } from 'vitest';
import { apiClient } from '../apiClient';
import { ApiError } from '../types';

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

const invalidCredentials = {
  success: false,
  error: { code: 'AUTH_INVALID_CREDENTIALS', message: 'Invalid email or password.' },
  meta: { timestamp: '2026-10-23T00:00:00Z' },
};

describe('apiClient 401 handling', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('surfaces the server message for a failed login instead of attempting a refresh', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(401, invalidCredentials));
    vi.stubGlobal('fetch', fetchMock);

    const error = await apiClient
      .post('/api/v1/auth/login', { email: 'a@b.c', password: 'x' })
      .catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).message).toBe('Invalid email or password.');
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(String(fetchMock.mock.calls[0][0])).toContain('/api/v1/auth/login');
  });

  it('still refreshes once on a 401 from a normal API call', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(jsonResponse(401, invalidCredentials))
      .mockResolvedValueOnce(
        jsonResponse(200, { success: true, data: { accessToken: 't' }, meta: {} }),
      )
      .mockResolvedValueOnce(jsonResponse(200, { success: true, data: [], meta: {} }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(apiClient.get('/api/v1/notes')).resolves.toEqual([]);
    expect(String(fetchMock.mock.calls[1][0])).toContain('/api/v1/auth/refresh');
  });
});

import { useAuthStore } from '@/features/auth/store/authStore';
import { ApiError, type ApiEnvelope } from './types';

// Implements /docs/08_Frontend_Architecture.md §3–4: unwraps the response envelope,
// and handles the 401 → refresh → retry flow with a single-flight guard so N
// concurrent 401s trigger exactly one /auth/refresh call, not N.

// Falls back to the standard local backend port instead of silently producing
// URLs like "undefined/api/v1/auth/login" when .env is missing — .env is (and
// should stay) gitignored since it's meant for secrets, which means it never
// survives being included in a fresh project download and has to be recreated
// every time. This fallback is a non-secret local-dev default, not a workaround
// that weakens anything — VITE_API_BASE_URL in .env still overrides it for
// staging/other setups; only a genuinely missing .env now degrades gracefully
// instead of breaking silently and confusingly.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
if (!import.meta.env.VITE_API_BASE_URL) {
  // eslint-disable-next-line no-console
  console.warn(
    '[DevTrack] VITE_API_BASE_URL is not set (missing .env?) — falling back to http://localhost:8080. ' +
      'Create a .env file at the repo root (copy .env.example) to remove this warning.',
  );
}

let refreshInFlight: Promise<void> | null = null;

async function refreshAccessToken(): Promise<void> {
  if (!refreshInFlight) {
    refreshInFlight = fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
      method: 'POST',
      credentials: 'include', // sends the httpOnly refresh cookie automatically
    })
      .then(async (res) => {
        if (!res.ok) {
          useAuthStore.getState().clear();
          throw new Error('Refresh failed');
        }
        const envelope = (await res.json()) as ApiEnvelope<{ accessToken: string }>;
        if (envelope.success) {
          useAuthStore.getState().setAccessToken(envelope.data.accessToken);
        }
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
}

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
}

async function request<T>(path: string, options: RequestOptions = {}, isRetry = false): Promise<T> {
  const accessToken = useAuthStore.getState().accessToken;

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...options.headers,
    },
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  // Single-flight refresh-and-retry — see module docblock.
  if (res.status === 401 && !isRetry) {
    await refreshAccessToken();
    return request<T>(path, options, true);
  }

  const envelope = (await res.json()) as ApiEnvelope<T>;

  if (!envelope.success) {
    throw new ApiError(envelope);
  }

  return envelope.data;
}

export const apiClient = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PATCH', body }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};

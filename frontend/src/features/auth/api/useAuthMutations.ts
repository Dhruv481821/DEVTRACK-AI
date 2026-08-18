import { useMutation } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import { useAuthStore } from '../store/authStore';
import type { LoginFormValues, RegisterFormValues } from '../schemas/authSchemas';

// Matches the backend's AuthResponse record exactly — will move to a generated
// type once openapi-typescript is wired up (08_Frontend_Architecture.md §3.1).
interface AuthResponse {
  accessToken: string;
}

export function useRegister() {
  return useMutation({
    mutationFn: (values: RegisterFormValues) => apiClient.post<void>('/api/v1/auth/register', values),
  });
}

export function useLogin() {
  const setAccessToken = useAuthStore((s) => s.setAccessToken);
  return useMutation({
    mutationFn: (values: LoginFormValues) => apiClient.post<AuthResponse>('/api/v1/auth/login', values),
    onSuccess: (data) => setAccessToken(data.accessToken),
  });
}

export function useLogout() {
  const clearAuth = useAuthStore((s) => s.clear);
  return useMutation({
    // FR-AUTH-04 — revokes the refresh token server-side (12_Security.md §2), not
    // just clearing local state, which would leave a still-valid refresh token
    // sitting in the httpOnly cookie.
    mutationFn: () => apiClient.post<void>('/api/v1/auth/logout'),
    onSettled: () => clearAuth(), // clear local state whether or not the network call succeeds
  });
}

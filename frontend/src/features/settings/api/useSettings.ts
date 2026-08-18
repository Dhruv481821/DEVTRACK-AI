import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

interface SettingsResponse {
  theme: string;
  notificationPrefs: Record<string, unknown>;
}

const SETTINGS_QUERY_KEY = ['settings', 'me'];

export function useSettings() {
  return useQuery({
    queryKey: SETTINGS_QUERY_KEY,
    queryFn: () => apiClient.get<SettingsResponse>('/api/v1/settings/me'),
  });
}

export function useUpdateSettings() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (theme: string) =>
      apiClient.patch<SettingsResponse>('/api/v1/settings/me', { theme, notificationPrefs: null }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: SETTINGS_QUERY_KEY }),
  });
}

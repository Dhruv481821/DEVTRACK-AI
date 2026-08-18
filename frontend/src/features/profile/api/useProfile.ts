import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { ProfileFormValues } from '../schemas/profileSchemas';

interface ProfileResponse {
  displayName: string | null;
  avatarUrl: string | null;
  bio: string | null;
}

const PROFILE_QUERY_KEY = ['profile', 'me'];

export function useProfile() {
  return useQuery({
    queryKey: PROFILE_QUERY_KEY,
    queryFn: () => apiClient.get<ProfileResponse>('/api/v1/profile/me'),
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: ProfileFormValues) => apiClient.patch<ProfileResponse>('/api/v1/profile/me', values),
    // Invalidate rather than manually patch the cache — simpler and correct here
    // since the server response is the full, authoritative post-update shape
    // (08_Frontend_Architecture.md §2's "mutate → invalidate query key" pattern).
    onSuccess: () => queryClient.invalidateQueries({ queryKey: PROFILE_QUERY_KEY }),
  });
}

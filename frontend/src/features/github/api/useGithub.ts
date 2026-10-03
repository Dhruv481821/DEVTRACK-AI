import { useMutation, useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors backend GithubConnectionResponse/RepoSnapshotResponse exactly
// (github/dto/response/*.java), per 08_Frontend_Architecture.md §3.
export interface GithubConnectionResponse {
  connected: boolean;
  githubUsername: string | null;
  lastSyncedAt: string | null;
}

export interface RepoSnapshotResponse {
  id: string;
  repoName: string;
  stars: number;
  primaryLanguage: string | null;
  commitsLast90Days: number;
  syncedAt: string;
}

const GITHUB_CONNECTION_QUERY_KEY = ['github', 'connection'];
const GITHUB_REPOS_QUERY_KEY = ['github', 'repos'];

export function useGithubConnection() {
  return useQuery({
    queryKey: GITHUB_CONNECTION_QUERY_KEY,
    queryFn: () => apiClient.get<GithubConnectionResponse>('/api/v1/github/connection'),
  });
}

// Only fetched once a connection exists — FR-GH-03's repo list is meaningless
// (and the backend returns an empty array anyway) before FR-GH-01's connect step.
export function useGithubRepos(enabled: boolean) {
  return useQuery({
    queryKey: GITHUB_REPOS_QUERY_KEY,
    queryFn: () => apiClient.get<RepoSnapshotResponse[]>('/api/v1/github/repos'),
    enabled,
  });
}

// No query invalidation on success: GitHub's OAuth callback does a full
// server-side redirect back to the frontend (GithubController#callback), which
// reloads the SPA and gives useGithubConnection a fresh fetch on mount — there's
// no stale client-side cache to invalidate across that round trip.
export function useConnectGithub() {
  return useMutation({
    mutationFn: () => apiClient.get<string>('/api/v1/github/oauth-url'),
    onSuccess: (url) => {
      window.location.href = url;
    },
  });
}
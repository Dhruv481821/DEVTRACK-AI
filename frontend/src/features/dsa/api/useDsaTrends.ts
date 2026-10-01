import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors the backend's DsaTrendsResponse exactly.
export interface DsaTrendsResponse {
  totalProblems: number;
  difficultyDistribution: Record<string, number>;
  tagFrequency: Record<string, number>;
}

export function useDsaTrends() {
  return useQuery({
    queryKey: ['dsa-trends'],
    queryFn: () => apiClient.get<DsaTrendsResponse>('/api/v1/dsa-problems/trends'),
  });
}
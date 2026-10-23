import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors backend ReadinessResponse exactly (FR-ANALYTICS-01).
export type ReadinessKey = 'DSA' | 'RESUME' | 'GITHUB';

export interface ReadinessComponent {
  key: ReadinessKey;
  label: string;
  score: number;
  signals: string[];
  nextStep: string | null;
}

export interface ReadinessTrendPoint {
  date: string;
  overall: number;
  dsa: number;
  resume: number;
  github: number;
}

export interface ReadinessResponse {
  overall: number;
  components: ReadinessComponent[];
  trend: ReadinessTrendPoint[];
  asOf: string;
}

export function useReadiness() {
  return useQuery({
    queryKey: ['analytics', 'readiness'],
    queryFn: () => apiClient.get<ReadinessResponse>('/api/v1/analytics/readiness'),
  });
}

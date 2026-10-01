import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors the backend's DsaAttemptResponse exactly.
export interface DsaAttemptResponse {
  id: string;
  attemptedAt: string;
  timeTakenMinutes: number | null;
  notes: string | null;
}

export interface CreateDsaAttemptValues {
  attemptedAt: string;
  timeTakenMinutes: number | null;
  notes: string | null;
}

const attemptsQueryKey = (problemId: string) => ['dsa-problems', problemId, 'attempts'];

// enabled lets ProblemRow fetch attempts only once a row is actually expanded, rather than
// eagerly fetching attempts for every problem in the list up front.
export function useDsaAttempts(problemId: string, enabled: boolean) {
  return useQuery({
    queryKey: attemptsQueryKey(problemId),
    queryFn: () =>
      apiClient.get<DsaAttemptResponse[]>(`/api/v1/dsa-problems/${problemId}/attempts`),
    enabled,
  });
}

export function useLogDsaAttempt(problemId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: CreateDsaAttemptValues) =>
      apiClient.post<DsaAttemptResponse>(`/api/v1/dsa-problems/${problemId}/attempts`, values),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: attemptsQueryKey(problemId) }),
  });
}
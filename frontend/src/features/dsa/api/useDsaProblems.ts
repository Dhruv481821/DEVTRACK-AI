import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { Difficulty } from '../schemas/dsaProblemSchema';

// Mirrors the backend's DsaProblemResponse exactly — hand-written interim, same as
// other features (useResumes.ts etc.) pending openapi-typescript being wired up.
export interface DsaProblemResponse {
  id: string;
  title: string;
  difficulty: Difficulty;
  tags: string[];
  createdAt: string;
}

export interface CreateDsaProblemValues {
  title: string;
  difficulty: Difficulty;
  tags: string[];
}

const DSA_PROBLEMS_QUERY_KEY = ['dsa-problems'];
const DSA_TRENDS_QUERY_KEY = ['dsa-trends'];

export function useDsaProblems() {
  return useQuery({
    queryKey: DSA_PROBLEMS_QUERY_KEY,
    queryFn: () => apiClient.get<DsaProblemResponse[]>('/api/v1/dsa-problems'),
  });
}

export function useCreateDsaProblem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: CreateDsaProblemValues) =>
      apiClient.post<DsaProblemResponse>('/api/v1/dsa-problems', values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: DSA_PROBLEMS_QUERY_KEY });
      // A new problem changes totalProblems/difficultyDistribution/tagFrequency too.
      queryClient.invalidateQueries({ queryKey: DSA_TRENDS_QUERY_KEY });
    },
  });
}
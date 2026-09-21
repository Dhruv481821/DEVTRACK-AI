import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { CreateResumeFormValues, UpdateResumeFormValues } from '../schemas/resumeSchemas';

// Mirrors the backend's ResumeResponse record exactly — hand-written for now, same
// interim as useProfile.ts/useAuthMutations.ts, per 08_Frontend_Architecture.md §3's
// not-yet-wired-up plan to replace these with openapi-typescript-generated types.
export interface ResumeResponse {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
}

const RESUMES_QUERY_KEY = ['resumes'];
const resumeQueryKey = (id: string) => ['resumes', id];

export function useResumes() {
  return useQuery({
    queryKey: RESUMES_QUERY_KEY,
    queryFn: () => apiClient.get<ResumeResponse[]>('/api/v1/resumes'),
  });
}

export function useResume(id: string | undefined) {
  return useQuery({
    queryKey: resumeQueryKey(id ?? ''),
    queryFn: () => apiClient.get<ResumeResponse>(`/api/v1/resumes/${id}`),
    enabled: !!id,
  });
}

export function useCreateResume() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: CreateResumeFormValues) =>
      apiClient.post<ResumeResponse>('/api/v1/resumes', values),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: RESUMES_QUERY_KEY }),
  });
}

export function useUpdateResume(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: UpdateResumeFormValues) =>
      apiClient.patch<ResumeResponse>(`/api/v1/resumes/${id}`, values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: RESUMES_QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: resumeQueryKey(id) });
    },
  });
}

export function useDeleteResume() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<void>(`/api/v1/resumes/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: RESUMES_QUERY_KEY }),
  });
}
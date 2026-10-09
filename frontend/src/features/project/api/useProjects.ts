import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { CreateProjectFormValues } from '../schemas/projectSchemas';

// Mirrors backend ProjectResponse exactly.
export interface ProjectResponse {
  id: string;
  name: string;
  description: string | null;
  linkedRepoSnapshotId: string | null;
  createdAt: string;
  updatedAt: string;
}

const PROJECTS_QUERY_KEY = ['projects'];

export function useProjects() {
  return useQuery({
    queryKey: PROJECTS_QUERY_KEY,
    queryFn: () => apiClient.get<ProjectResponse[]>('/api/v1/projects'),
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: CreateProjectFormValues) =>
      apiClient.post<ProjectResponse>('/api/v1/projects', values),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: PROJECTS_QUERY_KEY }),
  });
}

export function useDeleteProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) =>
      apiClient.delete<void>(`/api/v1/projects/${id}`),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: PROJECTS_QUERY_KEY }),
  });
}
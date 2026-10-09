import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { CreateTaskFormValues } from '../schemas/projectSchemas';

// Mirrors backend TaskResponse / TaskStatus exactly.
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface TaskResponse {
  id: string;
  title: string;
  status: TaskStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateTaskPayload {
  title?: string;
  status?: TaskStatus;
  notes?: string;
}

const tasksKey = (projectId: string) => ['projects', projectId, 'tasks'];
const tasksPath = (projectId: string) => `/api/v1/projects/${projectId}/tasks`;

// Fetch tasks only when the project card is expanded.
export function useTasks(projectId: string, enabled: boolean) {
  return useQuery({
    queryKey: tasksKey(projectId),
    queryFn: () => apiClient.get<TaskResponse[]>(tasksPath(projectId)),
    enabled,
  });
}

export function useCreateTask(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: CreateTaskFormValues) =>
      apiClient.post<TaskResponse>(tasksPath(projectId), values),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: tasksKey(projectId) }),
  });
}

export function useUpdateTask(projectId: string, taskId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateTaskPayload) =>
      apiClient.patch<TaskResponse>(
        `${tasksPath(projectId)}/${taskId}`,
        payload,
      ),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: tasksKey(projectId) }),
  });
}
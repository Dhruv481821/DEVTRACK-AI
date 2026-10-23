import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type {
  CreateStudyPlanFormValues,
  CreateStudyTaskFormValues,
} from '../schemas/studyPlannerSchemas';

// Mirrors backend StudyPlanResponse / StudyTaskResponse / StreakResponse exactly.
export interface StudyPlanResponse {
  id: string;
  title: string;
  targetDate: string | null;
  totalTasks: number;
  completedTasks: number;
  createdAt: string;
  updatedAt: string;
}

export interface StudyTaskResponse {
  id: string;
  studyPlanId: string;
  title: string;
  completed: boolean;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StreakResponse {
  currentStreak: number;
  longestStreak: number;
  lastActivityDate: string | null;
}

export interface UpdateStudyTaskPayload {
  title?: string;
  dueDate?: string;
  completed?: boolean;
}

const PLANS_KEY = ['study-plans'];
const STREAK_KEY = ['study-plans', 'streak'];
const tasksKey = (planId: string) => ['study-plans', planId, 'tasks'];
const tasksPath = (planId: string) => `/api/v1/study-plans/${planId}/tasks`;

export function useStudyPlans() {
  return useQuery({
    queryKey: PLANS_KEY,
    queryFn: () => apiClient.get<StudyPlanResponse[]>('/api/v1/study-plans'),
  });
}

export function useStudyStreak() {
  return useQuery({
    queryKey: STREAK_KEY,
    queryFn: () => apiClient.get<StreakResponse>('/api/v1/study-plans/streak'),
  });
}

export function useCreateStudyPlan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: CreateStudyPlanFormValues) =>
      apiClient.post<StudyPlanResponse>('/api/v1/study-plans', values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PLANS_KEY });
      // A target date creates a calendar event server-side (StudyPlanEventListener).
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });
}

export function useDeleteStudyPlan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<void>(`/api/v1/study-plans/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PLANS_KEY });
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });
}

// `enabled` lets a plan fetch its tasks only once expanded.
export function useStudyTasks(planId: string, enabled: boolean) {
  return useQuery({
    queryKey: tasksKey(planId),
    queryFn: () => apiClient.get<StudyTaskResponse[]>(tasksPath(planId)),
    enabled,
  });
}

// Task changes move the plan's progress counts and possibly the streak.
// PLANS_KEY is a prefix of both tasksKey(planId) and STREAK_KEY, so a single
// prefix invalidation refreshes tasks, plan counts and the streak together.
function useInvalidateStudyPlans() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: PLANS_KEY });
}

export function useCreateStudyTask(planId: string) {
  const invalidate = useInvalidateStudyPlans();
  return useMutation({
    mutationFn: (values: CreateStudyTaskFormValues) =>
      apiClient.post<StudyTaskResponse>(tasksPath(planId), values),
    onSuccess: invalidate,
  });
}

export function useUpdateStudyTask(planId: string, taskId: string) {
  const invalidate = useInvalidateStudyPlans();
  return useMutation({
    mutationFn: (payload: UpdateStudyTaskPayload) =>
      apiClient.patch<StudyTaskResponse>(`${tasksPath(planId)}/${taskId}`, payload),
    onSuccess: invalidate,
  });
}

export function useDeleteStudyTask(planId: string) {
  const invalidate = useInvalidateStudyPlans();
  return useMutation({
    mutationFn: (taskId: string) =>
      apiClient.delete<void>(`${tasksPath(planId)}/${taskId}`),
    onSuccess: invalidate,
  });
}

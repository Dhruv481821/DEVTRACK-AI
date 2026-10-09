import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { CreateJobApplicationFormValues } from '../schemas/jobTrackerSchemas';

// Mirrors backend JobApplicationResponse / JobApplicationStage exactly.
export type JobApplicationStage =
  | 'APPLIED'
  | 'SCREENING'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED';

export interface JobApplicationResponse {
  id: string;
  companyName: string;
  roleTitle: string;
  jobUrl: string | null;
  stage: JobApplicationStage;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

// Mirrors backend UpdateJobApplicationRequest — null/undefined = leave unchanged.
export interface UpdateJobApplicationPayload {
  companyName?: string;
  roleTitle?: string;
  jobUrl?: string;
  notes?: string;
  stage?: JobApplicationStage;
}

const JOB_APPLICATIONS_QUERY_KEY = ['job-applications'];

export function useJobApplications() {
  return useQuery({
    queryKey: JOB_APPLICATIONS_QUERY_KEY,
    queryFn: () =>
      apiClient.get<JobApplicationResponse[]>('/api/v1/job-applications'),
  });
}

export function useCreateJobApplication() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: CreateJobApplicationFormValues) =>
      apiClient.post<JobApplicationResponse>('/api/v1/job-applications', values),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: JOB_APPLICATIONS_QUERY_KEY }),
  });
}

export function useUpdateJobApplication(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateJobApplicationPayload) =>
      apiClient.patch<JobApplicationResponse>(
        `/api/v1/job-applications/${id}`,
        payload,
      ),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: JOB_APPLICATIONS_QUERY_KEY }),
  });
}
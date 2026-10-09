import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { CreateInterviewRoundPayload } from '../schemas/jobTrackerSchemas';

// Mirrors backend InterviewRoundResponse / InterviewOutcome exactly.
export type InterviewOutcome = 'PENDING' | 'PASSED' | 'FAILED';

export interface InterviewRoundResponse {
  id: string;
  roundName: string;
  occurredAt: string | null;
  outcome: InterviewOutcome;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateInterviewRoundPayload {
  roundName?: string;
  occurredAt?: string;
  outcome?: InterviewOutcome;
  notes?: string;
}

const roundsKey = (applicationId: string) => [
  'job-applications',
  applicationId,
  'interview-rounds',
];

const roundsPath = (applicationId: string) =>
  `/api/v1/job-applications/${applicationId}/interview-rounds`;

// `enabled` lets the card fetch rounds only once it is expanded.
export function useInterviewRounds(applicationId: string, enabled: boolean) {
  return useQuery({
    queryKey: roundsKey(applicationId),
    queryFn: () =>
      apiClient.get<InterviewRoundResponse[]>(roundsPath(applicationId)),
    enabled,
  });
}

export function useCreateInterviewRound(applicationId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateInterviewRoundPayload) =>
      apiClient.post<InterviewRoundResponse>(roundsPath(applicationId), payload),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: roundsKey(applicationId) }),
  });
}

export function useUpdateInterviewRound(applicationId: string, roundId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: UpdateInterviewRoundPayload) =>
      apiClient.patch<InterviewRoundResponse>(
        `${roundsPath(applicationId)}/${roundId}`,
        payload,
      ),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: roundsKey(applicationId) }),
  });
}
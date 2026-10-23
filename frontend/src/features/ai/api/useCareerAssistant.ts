import { useMutation } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors backend CareerAssistantResponse / CareerAssistantResult exactly. A
// quota-exceeded request is a 429 ApiError, never a status value here — same
// contract as the resume ATS score.
export type CareerAssistantStatus = 'SUCCESS' | 'CACHED_FALLBACK' | 'UNAVAILABLE';

export interface CareerAssistantResult {
  answer: string;
  recommendations: string[];
  // Computed server-side from which modules actually had data — not model-reported.
  basedOn: string[];
}

export interface CareerAssistantResponse {
  status: CareerAssistantStatus;
  result: CareerAssistantResult | null;
  generatedAt: string | null;
  message: string | null;
}

// Read-only on the backend, so nothing in the query cache needs invalidating.
export function useAskCareerAssistant() {
  return useMutation({
    mutationFn: (question: string) =>
      apiClient.post<CareerAssistantResponse>('/api/v1/ai/assistant', { question }),
  });
}

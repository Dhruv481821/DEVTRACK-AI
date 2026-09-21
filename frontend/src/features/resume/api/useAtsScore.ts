import { useMutation } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors the backend's AtsScoreResponse/AtsScoreResult/AtsScoreStatus exactly — same
// hand-written interim as useResumes.ts. Note QUOTA_EXCEEDED is deliberately not a value of
// AtsScoreStatus: on the backend it's a 429 thrown before a response body exists, so it never
// reaches this type — see useScoreResume's caller for how that's actually surfaced (a thrown
// ApiError with status 429, not a response with this status).
export type AtsScoreStatus = 'SUCCESS' | 'CACHED_FALLBACK' | 'UNAVAILABLE';

export interface AtsScoreResult {
  score: number;
  matchedKeywords: string[];
  missingKeywords: string[];
  formattingIssues: string[];
  reasoning: string;
  sourcesUsed: string[];
}

export interface AtsScoreResponse {
  status: AtsScoreStatus;
  result: AtsScoreResult | null;
  generatedAt: string | null;
  message: string | null;
}

// No cache invalidation on success — scoring a resume doesn't change the resume's own data,
// so there's nothing in the query cache that needs to be refreshed.
export function useScoreResume(resumeId: string) {
  return useMutation({
    mutationFn: (jobDescription: string) =>
      apiClient.post<AtsScoreResponse>(`/api/v1/resumes/${resumeId}/ats-score`, {
        jobDescription,
      }),
  });
}
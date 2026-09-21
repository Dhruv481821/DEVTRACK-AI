import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { SectionType } from '../schemas/sectionSchemas';

// Mirrors the backend's ResumeSectionResponse record exactly — same hand-written
// interim as useResumes.ts.
export interface ResumeSectionResponse {
  id: string;
  sectionType: SectionType;
  content: Record<string, unknown>;
  orderIndex: number;
}

export interface CreateSectionValues {
  sectionType: SectionType;
  content: Record<string, unknown>;
  orderIndex: number;
}

export interface UpdateSectionValues {
  content?: Record<string, unknown>;
  orderIndex?: number;
}

const sectionsQueryKey = (resumeId: string) => ['resumes', resumeId, 'sections'];

export function useResumeSections(resumeId: string) {
  return useQuery({
    queryKey: sectionsQueryKey(resumeId),
    queryFn: () => apiClient.get<ResumeSectionResponse[]>(`/api/v1/resumes/${resumeId}/sections`),
    enabled: !!resumeId,
  });
}

export function useCreateSection(resumeId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: CreateSectionValues) =>
      apiClient.post<ResumeSectionResponse>(`/api/v1/resumes/${resumeId}/sections`, values),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: sectionsQueryKey(resumeId) }),
  });
}

export function useUpdateSection(resumeId: string, sectionId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (values: UpdateSectionValues) =>
      apiClient.patch<ResumeSectionResponse>(
        `/api/v1/resumes/${resumeId}/sections/${sectionId}`,
        values,
      ),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: sectionsQueryKey(resumeId) }),
  });
}
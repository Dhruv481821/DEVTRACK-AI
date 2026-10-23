import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type { TiptapNode } from '../lib/noteContent';

// Mirrors backend NoteResponse / CreateNoteRequest / UpdateNoteRequest exactly.
export interface NoteResponse {
  id: string;
  title: string;
  content: TiptapNode | null;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NotePayload {
  title?: string;
  content?: TiptapNode;
  tagNames?: string[];
}

const NOTES_QUERY_KEY = ['notes'];

// GET /notes is paginated (ApiEnvelope.paginated). One generous page, newest
// first, is enough for a personal notes list; apiClient returns the data array.
export function useNotes() {
  return useQuery({
    queryKey: NOTES_QUERY_KEY,
    queryFn: () =>
      apiClient.get<NoteResponse[]>('/api/v1/notes?size=100&sort=updatedAt,desc'),
  });
}

export function useCreateNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: NotePayload) =>
      apiClient.post<NoteResponse>('/api/v1/notes', payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: NOTES_QUERY_KEY }),
  });
}

export function useUpdateNote(id: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: NotePayload) =>
      apiClient.patch<NoteResponse>(`/api/v1/notes/${id}`, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: NOTES_QUERY_KEY }),
  });
}

export function useDeleteNote() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => apiClient.delete<void>(`/api/v1/notes/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: NOTES_QUERY_KEY }),
  });
}

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';
import type {
  CreateCertificateFormValues,
  UpdateCertificateFormValues,
} from '../schemas/certificateSchemas';

export interface CertificateResponse {
  id: string;
  name: string;
  issuingOrg: string;
  issueDate: string | null;
  verificationUrl: string | null;
}

const CERTIFICATES_QUERY_KEY = ['certificates'];

export function useCertificates() {
  return useQuery({
    queryKey: CERTIFICATES_QUERY_KEY,
    queryFn: () => apiClient.get<CertificateResponse[]>('/api/v1/certificates'),
  });
}

export function useCreateCertificate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: CreateCertificateFormValues) =>
      apiClient.post<CertificateResponse>('/api/v1/certificates', values),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: CERTIFICATES_QUERY_KEY }),
  });
}

export function useUpdateCertificate(id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: UpdateCertificateFormValues) =>
      apiClient.patch<CertificateResponse>(
        `/api/v1/certificates/${id}`,
        values,
      ),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: CERTIFICATES_QUERY_KEY }),
  });
}

export function useDeleteCertificate() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) =>
      apiClient.delete<void>(`/api/v1/certificates/${id}`),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: CERTIFICATES_QUERY_KEY }),
  });
}
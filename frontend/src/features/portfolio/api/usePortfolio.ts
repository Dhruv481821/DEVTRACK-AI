import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';

import { apiClient } from '@/shared/api-client/apiClient';
import type { PortfolioSettingsFormValues } from '../schemas/portfolioSchemas';

export interface PortfolioSettingsResponse {
  slug: string | null;
  headline: string | null;
  published: boolean;
}

export interface PortfolioPublicView {
  displayName: string | null;
  headline: string | null;
  bio: string | null;
  avatarUrl: string | null;

  projects: {
    name: string;
    description: string | null;
  }[];

  certificates: {
    name: string;
    issuingOrg: string;
    issueDate: string | null;
    verificationUrl: string | null;
  }[];

  github: {
    repoCount: number;
    totalStars: number;
    topLanguages: string[];
  } | null;
}

const MY_PORTFOLIO_QUERY_KEY = ['portfolio', 'me'];

export function useMyPortfolio() {
  return useQuery({
    queryKey: MY_PORTFOLIO_QUERY_KEY,
    queryFn: () =>
      apiClient.get<PortfolioSettingsResponse>(
        '/api/v1/portfolio/me',
      ),
  });
}

export function useUpdateMyPortfolio() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (values: PortfolioSettingsFormValues) =>
      apiClient.patch<PortfolioSettingsResponse>(
        '/api/v1/portfolio/me',
        values,
      ),

    onSuccess: () =>
      queryClient.invalidateQueries({
        queryKey: MY_PORTFOLIO_QUERY_KEY,
      }),
  });
}

export function usePublicPortfolio(slug: string) {
  return useQuery({
    queryKey: ['portfolio', 'public', slug],

    queryFn: () =>
      apiClient.get<PortfolioPublicView>(
        `/api/v1/portfolio/${encodeURIComponent(slug)}`,
      ),

    retry: false,
  });
}
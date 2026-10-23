import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api-client/apiClient';

// Mirrors backend BillingStatusResponse / CreateOrderResponse / AiQuotaResponse exactly.
export type Tier = 'FREE' | 'PRO';
export type BillingPlan = 'PRO_MONTHLY' | 'PRO_YEARLY';

export interface PlanOption {
  plan: BillingPlan;
  label: string;
  amountPaise: number;
  currency: string;
}

export interface BillingStatus {
  tier: Tier;
  proUntil: string | null;
  enabled: boolean;
  keyId: string | null;
  plans: PlanOption[];
}

export interface CreateOrderResponse {
  orderId: string;
  amountPaise: number;
  currency: string;
  keyId: string;
  plan: BillingPlan;
  label: string;
}

export interface VerifyPaymentPayload {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

export interface AiQuota {
  tier: Tier;
  dailyLimit: number;
  freeDailyLimit: number;
  proDailyLimit: number;
}

const STATUS_KEY = ['billing', 'status'];
const QUOTA_KEY = ['ai', 'quota'];

export function useBillingStatus() {
  return useQuery({
    queryKey: STATUS_KEY,
    queryFn: () => apiClient.get<BillingStatus>('/api/v1/billing/status'),
  });
}

export function useAiQuota() {
  return useQuery({
    queryKey: QUOTA_KEY,
    queryFn: () => apiClient.get<AiQuota>('/api/v1/ai/quota'),
  });
}

export function useCreateOrder() {
  return useMutation({
    mutationFn: (plan: BillingPlan) =>
      apiClient.post<CreateOrderResponse>('/api/v1/billing/orders', { plan }),
  });
}

export function useVerifyPayment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: VerifyPaymentPayload) =>
      apiClient.post<BillingStatus>('/api/v1/billing/orders/verify', payload),
    onSuccess: (status) => {
      queryClient.setQueryData(STATUS_KEY, status);
      queryClient.invalidateQueries({ queryKey: QUOTA_KEY });
    },
  });
}

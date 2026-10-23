import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import {
  useAiQuota,
  useBillingStatus,
  useCreateOrder,
  useVerifyPayment,
  type BillingPlan,
} from '../api/useBilling';
import { formatInr, loadRazorpay, openCheckout } from '../lib/razorpayCheckout';

type Notice = { tone: 'success' | 'error' | 'muted'; text: string } | null;

export function BillingPanel() {
  const { data: status, isLoading, isError } = useBillingStatus();
  const { data: quota } = useAiQuota();
  const createOrder = useCreateOrder();
  const verifyPayment = useVerifyPayment();
  const [pendingPlan, setPendingPlan] = useState<BillingPlan | null>(null);
  const [notice, setNotice] = useState<Notice>(null);

  const buy = async (plan: BillingPlan) => {
    setNotice(null);
    setPendingPlan(plan);
    try {
      const [order] = await Promise.all([createOrder.mutateAsync(plan), loadRazorpay()]);
      openCheckout(
        {
          key: order.keyId,
          amount: order.amountPaise,
          currency: order.currency,
          order_id: order.orderId,
          name: 'DevTrack AI',
          description: order.label,
          theme: { color: '#6E5BFF' },
          handler: (response) => {
            verifyPayment.mutate(
              {
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
              },
              {
                onSuccess: () => setNotice({ tone: 'success', text: 'Payment received — Pro is active.' }),
                onError: () =>
                  setNotice({
                    tone: 'error',
                    text: "We couldn't confirm the payment yet. If you were charged, Pro will activate automatically within a few minutes.",
                  }),
                onSettled: () => setPendingPlan(null),
              },
            );
          },
          modal: { ondismiss: () => setPendingPlan(null) },
        },
        (message) => {
          setPendingPlan(null);
          setNotice({ tone: 'error', text: message });
        },
      );
    } catch {
      setPendingPlan(null);
      setNotice({ tone: 'error', text: "Couldn't start checkout. Please try again." });
    }
  };

  if (isLoading) {
    return <p className="font-body text-sm text-text-muted">Loading your plan…</p>;
  }
  if (isError || !status) {
    return (
      <p className="font-body text-sm text-danger">
        Couldn&apos;t load your plan. Please try again.
      </p>
    );
  }

  const isPro = status.tier === 'PRO';

  return (
    <div className="space-y-6">
      <Card>
        <p className="font-body text-xs uppercase tracking-wide text-text-muted">Current plan</p>
        <p className="mt-1 font-display text-2xl">{isPro ? 'Pro' : 'Free'}</p>
        {isPro && status.proUntil && (
          <p className="font-body text-sm text-text-muted">
            Active until {new Date(status.proUntil).toLocaleDateString(undefined, { dateStyle: 'long' })}.
            Buying another pass adds to this date.
          </p>
        )}
        {quota && (
          <p className="mt-2 font-body text-sm text-text-primary">
            {quota.dailyLimit} AI requests a day (AI Assistant and resume scoring).
          </p>
        )}
      </Card>

      <div>
        <h2 className="mb-1 font-display text-lg">{isPro ? 'Extend Pro' : 'Upgrade to Pro'}</h2>
        <p className="mb-4 font-body text-sm text-text-muted">
          Every tracker stays free. Pro raises your AI allowance
          {quota ? ` from ${quota.freeDailyLimit} to ${quota.proDailyLimit} requests a day` : ''}.
          One-time payment, no auto-renewal. Pay by UPI, card or netbanking. By buying you agree to
          the{' '}
          <Link to="/terms" className="text-signal hover:underline">
            Terms
          </Link>{' '}
          and{' '}
          <Link to="/refunds" className="text-signal hover:underline">
            Refund policy
          </Link>{' '}
          (full refund within 7 days).
        </p>

        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {status.plans.map((option) => {
            const yearly = option.plan === 'PRO_YEARLY';
            return (
              <Card key={option.plan} className="flex flex-col gap-3">
                <div>
                  <p className="font-body text-sm text-text-muted">{option.label}</p>
                  <p className="font-display text-3xl">
                    {formatInr(option.amountPaise)}
                    <span className="ml-1 font-body text-sm text-text-muted">
                      {yearly ? '/ year' : '/ month'}
                    </span>
                  </p>
                  {yearly && (
                    <p className="font-body text-xs text-text-muted">About 2 months free vs monthly.</p>
                  )}
                </div>
                <Button
                  type="button"
                  className="mt-auto"
                  disabled={!status.enabled || pendingPlan !== null}
                  loading={pendingPlan === option.plan}
                  onClick={() => buy(option.plan)}
                >
                  {isPro ? 'Add' : 'Buy'} {yearly ? '12 months' : '1 month'}
                </Button>
              </Card>
            );
          })}
        </div>

        {!status.enabled && (
          <p className="mt-3 font-body text-sm text-text-muted">
            Payments aren&apos;t switched on for this server yet.
          </p>
        )}
      </div>

      {notice && (
        <p
          role="status"
          className={
            notice.tone === 'success'
              ? 'font-body text-sm text-success'
              : notice.tone === 'error'
                ? 'font-body text-sm text-danger'
                : 'font-body text-sm text-text-muted'
          }
        >
          {notice.text}
        </p>
      )}
    </div>
  );
}

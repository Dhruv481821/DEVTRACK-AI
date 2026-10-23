import { BillingPanel } from '@/features/billing/components/BillingPanel';

export function BillingPage() {
  return (
    <div className="max-w-3xl">
      <h1 className="mb-6 font-display text-2xl">Plan &amp; billing</h1>
      <BillingPanel />
    </div>
  );
}

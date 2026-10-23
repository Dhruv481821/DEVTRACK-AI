import { Link } from 'react-router-dom';
import { Card } from '@/shared/ui/Card';
import { LegalLayout } from '@/features/legal/components/LegalLayout';
import { PUBLIC_PRICING } from '@/shared/config/business';

const inr = (n: number) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

export function PricingPage() {
  return (
    <LegalLayout title="Pricing">
      <p className="text-text-muted">
        DevTrack AI is a workspace for developers: DSA practice tracking, notes, calendar and study
        planner, resume builder, job and interview tracker, projects, certificates, GitHub
        analytics, a public portfolio page and AI career insights.
      </p>

      <div className="grid grid-cols-1 gap-4 pt-2 sm:grid-cols-2">
        <Card className="space-y-2">
          <p className="font-display text-xl">Free</p>
          <p className="font-display text-3xl">{inr(0)}</p>
          <ul className="list-inside list-disc text-text-muted">
            <li>Every tracker and the public portfolio</li>
            <li>{PUBLIC_PRICING.freeAiPerDay} AI requests a day</li>
          </ul>
        </Card>
        <Card className="space-y-2">
          <p className="font-display text-xl">Pro</p>
          <p className="font-display text-3xl">
            {inr(PUBLIC_PRICING.proMonthlyInr)}
            <span className="ml-1 font-body text-sm text-text-muted">/ month</span>
          </p>
          <p className="text-text-muted">or {inr(PUBLIC_PRICING.proYearlyInr)} for 12 months</p>
          <ul className="list-inside list-disc text-text-muted">
            <li>Everything in Free</li>
            <li>{PUBLIC_PRICING.proAiPerDay} AI requests a day (AI Assistant and resume scoring)</li>
          </ul>
        </Card>
      </div>

      <p className="text-text-muted">
        Pro is a one-time prepaid pass, not a subscription: nothing renews automatically. Prices are
        in Indian Rupees. Payments are processed by Razorpay (UPI, cards, netbanking). See the{' '}
        <Link to="/refunds" className="text-signal hover:underline">
          refund policy
        </Link>
        .
      </p>
      <p>
        <Link to="/register" className="text-signal hover:underline">
          Create a free account →
        </Link>
      </p>
    </LegalLayout>
  );
}

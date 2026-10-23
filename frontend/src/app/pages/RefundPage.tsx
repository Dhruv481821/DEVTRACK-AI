import { LegalLayout, Section, SupportEmail, Updated } from '@/features/legal/components/LegalLayout';

export function RefundPage() {
  return (
    <LegalLayout title="Refund & Cancellation Policy">
      <Updated />

      <Section title="What you buy">
        <p>
          Pro is a one-time prepaid pass for 1 month or 12 months. It is a digital service: there is
          nothing to ship, and Pro activates on your account as soon as payment is confirmed —
          usually instantly, at most within a few minutes.
        </p>
      </Section>

      <Section title="Cancellation">
        <p>
          Passes never renew automatically, so there is nothing to cancel. When a pass ends your
          account returns to the Free plan and all your data stays.
        </p>
      </Section>

      <Section title="Refunds">
        <ul className="list-inside list-disc space-y-1">
          <li>
            <strong>Within 7 days of purchase:</strong> full refund on request, no questions asked.
          </li>
          <li>
            <strong>Charged but Pro not activated, or charged twice:</strong> full refund of the
            affected payment, at any time.
          </li>
          <li>
            After 7 days, passes are non-refundable, except as required by law or where we close your
            account without a breach on your part (unused portion refunded).
          </li>
        </ul>
      </Section>

      <Section title="How to request">
        <p>
          Email <SupportEmail /> from your account email with the payment date and amount (the
          Razorpay payment ID from your receipt helps). Approved refunds are issued through Razorpay
          to the original payment method within 5–7 business days; your bank may take longer to show
          it.
        </p>
      </Section>
    </LegalLayout>
  );
}

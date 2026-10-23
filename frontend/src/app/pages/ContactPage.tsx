import { LegalLayout, Section } from '@/features/legal/components/LegalLayout';
import { BUSINESS } from '@/shared/config/business';

export function ContactPage() {
  return (
    <LegalLayout title="Contact">
      <dl className="space-y-3">
        <div>
          <dt className="text-text-muted">Business</dt>
          <dd>{BUSINESS.name}</dd>
        </div>
        <div>
          <dt className="text-text-muted">Operated by</dt>
          <dd>{BUSINESS.operator}</dd>
        </div>
        <div>
          <dt className="text-text-muted">Location</dt>
          <dd>
            {BUSINESS.city}, {BUSINESS.state}, {BUSINESS.country}
          </dd>
        </div>
        <div>
          <dt className="text-text-muted">Email</dt>
          <dd>
            {BUSINESS.supportEmail ? (
              <a href={`mailto:${BUSINESS.supportEmail}`} className="text-signal hover:underline">
                {BUSINESS.supportEmail}
              </a>
            ) : (
              <span className="text-text-muted">Not configured (set VITE_SUPPORT_EMAIL)</span>
            )}
          </dd>
        </div>
      </dl>

      <Section title="Support">
        <p>
          We reply within 2 business days. For billing questions, include your payment date and
          amount. For account deletion or data requests, write from your account email.
        </p>
      </Section>
    </LegalLayout>
  );
}

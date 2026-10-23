import { Link } from 'react-router-dom';
import { LegalLayout, Section, SupportEmail, Updated } from '@/features/legal/components/LegalLayout';
import { BUSINESS } from '@/shared/config/business';

export function TermsPage() {
  return (
    <LegalLayout title="Terms of Service">
      <Updated />
      <p>
        These terms govern your use of DevTrack AI (the &quot;Service&quot;), operated by{' '}
        {BUSINESS.operator} under the name {BUSINESS.name}, {BUSINESS.city}, {BUSINESS.state},{' '}
        {BUSINESS.country} (&quot;we&quot;, &quot;us&quot;). By creating an account or using the
        Service you agree to these terms.
      </p>

      <Section title="1. The Service">
        <p>
          DevTrack AI lets you track DSA practice, notes, calendar events, study plans, resumes, job
          applications, interviews, projects and certificates, connect GitHub for repository
          statistics, publish an optional public portfolio page and get AI-generated insights based
          on your own data.
        </p>
      </Section>

      <Section title="2. Your account">
        <p>
          You must provide accurate information, keep your password secure and are responsible for
          activity under your account. You must be at least 18 years old, or have the consent of a
          parent or guardian, to use the Service. Tell us promptly at <SupportEmail /> if you
          suspect unauthorised access.
        </p>
      </Section>

      <Section title="3. Your content">
        <p>
          You own the content you add. You grant us a limited licence to store, process and display
          it only to operate the Service for you — including sending relevant parts to our AI
          provider when you use an AI feature, and showing your published portfolio publicly when
          you choose to publish it. You can unpublish your portfolio at any time.
        </p>
      </Section>

      <Section title="4. Acceptable use">
        <p>
          Do not misuse the Service: no unlawful content, no attempts to access other users&apos;
          data, no disrupting or overloading the Service, no automated scraping, and no attempts to
          circumvent usage limits or payment.
        </p>
      </Section>

      <Section title="5. AI features">
        <p>
          AI insights (resume scoring, the AI Assistant) are generated automatically and may be
          incomplete or wrong. They are informational only and are not professional career, legal or
          financial advice. Use your own judgement before acting on them. AI usage is subject to the
          daily limits of your plan.
        </p>
      </Section>

      <Section title="6. Plans and payments">
        <p>
          The Free plan is free. Pro is sold as a prepaid pass for a fixed period (1 or 12 months)
          at the prices shown on the{' '}
          <Link to="/pricing" className="text-signal hover:underline">
            pricing page
          </Link>
          , in Indian Rupees. Passes do not renew automatically. Payments are processed by Razorpay;
          we do not receive or store your card, UPI or bank details. Pro features activate as soon
          as payment is confirmed. Refunds are covered by our{' '}
          <Link to="/refunds" className="text-signal hover:underline">
            refund policy
          </Link>
          . We may change prices for future purchases; a pass you have already bought is not
          affected.
        </p>
      </Section>

      <Section title="7. Availability">
        <p>
          We work to keep the Service available but do not guarantee it will be uninterrupted or
          error-free. Features may change. If AI features are temporarily unavailable, the rest of
          the Service continues to work.
        </p>
      </Section>

      <Section title="8. Suspension and termination">
        <p>
          You may stop using the Service at any time and ask us to delete your account. We may
          suspend or close accounts that breach these terms. If we close your account without a
          breach on your part, we will refund the unused portion of any active Pro pass.
        </p>
      </Section>

      <Section title="9. Limitation of liability">
        <p>
          To the extent permitted by law, the Service is provided &quot;as is&quot;, and our total
          liability for any claim relating to the Service is limited to the amount you paid us in
          the 12 months before the claim. We are not liable for indirect or consequential losses,
          including lost opportunities or decisions made based on AI insights.
        </p>
      </Section>

      <Section title="10. Governing law">
        <p>
          These terms are governed by the laws of India. Courts at {BUSINESS.city}, {BUSINESS.state}{' '}
          have jurisdiction over any dispute.
        </p>
      </Section>

      <Section title="11. Changes and contact">
        <p>
          We may update these terms; the date above shows the latest version, and material changes
          will be notified in the app. Questions: <SupportEmail />.
        </p>
      </Section>
    </LegalLayout>
  );
}

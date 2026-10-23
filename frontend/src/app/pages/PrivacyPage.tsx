import { LegalLayout, Section, SupportEmail, Updated } from '@/features/legal/components/LegalLayout';
import { BUSINESS } from '@/shared/config/business';

export function PrivacyPage() {
  return (
    <LegalLayout title="Privacy Policy">
      <Updated />
      <p>
        This policy explains what DevTrack AI ({BUSINESS.name}, operated by {BUSINESS.operator},{' '}
        {BUSINESS.city}, {BUSINESS.state}, {BUSINESS.country}) collects, why, and your choices. We
        process personal data in line with India&apos;s Digital Personal Data Protection Act, 2023.
      </p>

      <Section title="What we collect">
        <ul className="list-inside list-disc space-y-1">
          <li>
            <strong>Account:</strong> email address and a hashed password, or your Google account
            identity if you sign in with Google.
          </li>
          <li>
            <strong>Profile and content you add:</strong> display name, bio, notes, calendar events,
            study plans, DSA problems, resumes, job applications, interview rounds, projects and
            certificates.
          </li>
          <li>
            <strong>GitHub (optional):</strong> if you connect GitHub, an access token (stored
            encrypted) and statistics for repositories you own — names, stars, primary language and
            recent commit counts.
          </li>
          <li>
            <strong>Payments:</strong> Razorpay order and payment IDs, plan, amount and status. We
            never receive your card, UPI or bank details.
          </li>
          <li>
            <strong>Technical:</strong> IP address for security rate limiting, and server logs.
          </li>
        </ul>
      </Section>

      <Section title="How we use it">
        <p>
          Only to provide the Service: storing and showing your data to you, generating AI insights
          you request, publishing your portfolio if you choose to, processing payments, securing
          accounts and sending account emails (verification, password reset). We do not sell your
          data and do not use it for advertising.
        </p>
      </Section>

      <Section title="Who we share it with">
        <p>Service providers that process data on our behalf, only as needed:</p>
        <ul className="list-inside list-disc space-y-1">
          <li>Google (Gemini API) — the relevant parts of your data when you use an AI feature.</li>
          <li>Razorpay — payment processing.</li>
          <li>Resend — sending account emails.</li>
          <li>Hosting and databases (Vercel, Railway, Neon) — storing and serving the app.</li>
          <li>GitHub and Google — only when you choose to sign in with or connect them.</li>
        </ul>
        <p>
          Some providers may process data outside India. We may also disclose data where required by
          law.
        </p>
      </Section>

      <Section title="Public portfolio">
        <p>
          Your portfolio page is private until you publish it. When published, it shows only your
          display name, headline, bio, avatar, project names and descriptions, certificates and
          GitHub totals. It never shows your email, notes, tasks, applications or repository names.
          You can unpublish it at any time.
        </p>
      </Section>

      <Section title="Cookies and storage">
        <p>
          We use one essential, secure http-only cookie to keep you signed in. No advertising or
          analytics cookies.
        </p>
      </Section>

      <Section title="Retention and your rights">
        <p>
          We keep your data while your account is active. You can access and correct most data
          directly in the app. To request a copy of your data, deletion of your account, or to raise
          a grievance, email <SupportEmail />. We act on deletion requests within 30 days, except
          where we must keep payment records by law.
        </p>
      </Section>

      <Section title="Security">
        <p>
          Passwords are hashed, GitHub tokens are encrypted, connections use HTTPS, and access to your
          data is restricted to your account. No system is perfectly secure; tell us at{' '}
          <SupportEmail /> if you find a problem.
        </p>
      </Section>

      <Section title="Changes">
        <p>We will update the date above when this policy changes.</p>
      </Section>
    </LegalLayout>
  );
}

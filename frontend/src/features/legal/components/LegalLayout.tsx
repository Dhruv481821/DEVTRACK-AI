import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { BUSINESS } from '@/shared/config/business';
import { LegalFooter } from './LegalFooter';

// Public shell for pricing and policy pages: readable line length, no app chrome.
export function LegalLayout({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="min-h-screen px-4 py-10">
      <div className="mx-auto max-w-2xl">
        <header className="mb-10 flex items-center justify-between gap-4">
          <Link to="/login" className="font-display text-lg text-text-primary">
            DevTrack AI
          </Link>
          <Link to="/login" className="font-body text-sm text-signal hover:underline">
            Log in
          </Link>
        </header>

        <main>
          <h1 className="mb-2 font-display text-3xl">{title}</h1>
          <div className="legal-prose space-y-4 font-body text-sm leading-relaxed text-text-primary">
            {children}
          </div>
        </main>

        <footer className="mt-16 space-y-3 border-t border-border pt-6 text-center">
          <LegalFooter />
          <p className="font-body text-xs text-text-muted">
            © {new Date().getFullYear()} {BUSINESS.name} · {BUSINESS.city}, {BUSINESS.state},{' '}
            {BUSINESS.country}
          </p>
        </footer>
      </div>
    </div>
  );
}

export function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="space-y-2 pt-4">
      <h2 className="font-display text-lg">{title}</h2>
      {children}
    </section>
  );
}

export function Updated() {
  return <p className="text-text-muted">Last updated: {BUSINESS.policiesUpdated}</p>;
}

export function SupportEmail() {
  if (!BUSINESS.supportEmail) {
    return <span className="text-text-muted">our support email (listed on the Contact page)</span>;
  }
  return (
    <a href={`mailto:${BUSINESS.supportEmail}`} className="text-signal hover:underline">
      {BUSINESS.supportEmail}
    </a>
  );
}

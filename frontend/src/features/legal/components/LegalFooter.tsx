import { Link } from 'react-router-dom';

const LINKS = [
  { to: '/pricing', label: 'Pricing' },
  { to: '/terms', label: 'Terms' },
  { to: '/privacy', label: 'Privacy' },
  { to: '/refunds', label: 'Refunds' },
  { to: '/contact', label: 'Contact' },
];

export function LegalFooter() {
  return (
    <nav aria-label="Legal" className="flex flex-wrap justify-center gap-x-4 gap-y-1">
      {LINKS.map((l) => (
        <Link key={l.to} to={l.to} className="font-body text-xs text-text-muted hover:text-text-primary">
          {l.label}
        </Link>
      ))}
    </nav>
  );
}

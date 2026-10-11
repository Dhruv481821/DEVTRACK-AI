import { Card } from '@/shared/ui/Card';
import { PortfolioSettingsForm } from '@/features/portfolio/components/PortfolioSettingsForm';

export function PortfolioPage() {
  return (
    <div>
      <h1 className="mb-2 font-display text-2xl">Portfolio</h1>
      <p className="mb-6 font-body text-sm text-text-muted">
        Your public page is built from your profile, projects, certificates and
        GitHub stats — nothing is retyped.
      </p>
      <Card>
        <PortfolioSettingsForm />
      </Card>
    </div>
  );
}
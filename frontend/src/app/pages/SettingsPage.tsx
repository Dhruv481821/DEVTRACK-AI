import { useSearchParams } from 'react-router-dom';
import { SettingsForm } from '@/features/settings/components/SettingsForm';
import { GithubConnectCard } from '@/features/github/components/GithubConnectCard';

// The GitHub connect card lives here, not on a dedicated route — GithubController's
// OAuth callback redirects to /settings?github=connected (its own existing
// behavior, unchanged by this addition), so this is where the flow actually lands.
export function SettingsPage() {
  const [searchParams] = useSearchParams();
  const justConnected = searchParams.get('github') === 'connected';

  return (
    <div className="space-y-8">
      <div>
        <h1 className="mb-6 font-display text-2xl">Settings</h1>
        <SettingsForm />
      </div>

      <div>
        {justConnected && (
          <p className="mb-3 font-body text-sm text-success">GitHub connected.</p>
        )}
        <GithubConnectCard />
      </div>
    </div>
  );
}
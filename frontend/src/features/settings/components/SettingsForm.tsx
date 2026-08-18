import { Button } from '@/shared/ui/Button';
import { useSettings, useUpdateSettings } from '../api/useSettings';

export function SettingsForm() {
  const { data: settings, isLoading } = useSettings();
  const updateMutation = useUpdateSettings();

  if (isLoading) {
    return <p className="font-body text-sm text-text-muted">Loading settings…</p>;
  }

  return (
    <div className="max-w-md space-y-4">
      <div>
        <p className="mb-2 font-body text-sm font-medium text-text-primary">Theme</p>
        <div className="flex gap-2">
          {(['dark', 'light'] as const).map((theme) => (
            <Button
              key={theme}
              type="button"
              onClick={() => updateMutation.mutate(theme)}
              loading={updateMutation.isPending && updateMutation.variables === theme}
              className={settings?.theme === theme ? '' : 'bg-surface-raised text-text-muted'}
            >
              {theme === 'dark' ? 'Dark' : 'Light'}
            </Button>
          ))}
        </div>
        {/* Light theme is a documented v1 goal (PRD tech stack: "Dark First,
            Optional Light Theme") but the actual light-mode token values haven't
            been designed yet (10_UI_UX_Design_System.md is dark-only so far) — this
            toggle persists the preference correctly, but selecting "Light" won't
            visibly change anything until that design pass happens. */}
      </div>
      {updateMutation.isSuccess && <p className="font-body text-sm text-success">Saved.</p>}
    </div>
  );
}

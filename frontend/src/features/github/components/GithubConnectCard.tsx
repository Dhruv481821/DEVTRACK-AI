import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { useGithubConnection, useConnectGithub } from '../api/useGithub';

export function GithubConnectCard() {
  const { data: connection, isLoading, isError } = useGithubConnection();
  const connectMutation = useConnectGithub();

  if (isLoading) {
    return <p className="font-body text-sm text-text-muted">Loading GitHub connection…</p>;
  }

  if (isError) {
    return (
      <p className="font-body text-sm text-danger">
        Couldn&apos;t load your GitHub connection. Please try again.
      </p>
    );
  }

  return (
    <Card>
      <p className="mb-2 font-body text-sm font-medium text-text-primary">GitHub</p>
      {connection?.connected ? (
        <p className="font-body text-sm text-text-muted">
          Connected as {connection.githubUsername}
          {connection.lastSyncedAt
            ? ` · last synced ${new Date(connection.lastSyncedAt).toLocaleString()}`
            : ' · not synced yet (first sync runs within an hour)'}
        </p>
      ) : (
        <>
          <p className="mb-3 font-body text-sm text-text-muted">
            Connect your GitHub account to see your repo activity on the dashboard.
          </p>
          <Button
            type="button"
            loading={connectMutation.isPending}
            onClick={() => connectMutation.mutate()}
          >
            Connect GitHub
          </Button>
          {connectMutation.isError && (
            <p className="mt-2 font-body text-sm text-danger">
              Couldn&apos;t start the GitHub connection. Please try again.
            </p>
          )}
        </>
      )}
    </Card>
  );
}
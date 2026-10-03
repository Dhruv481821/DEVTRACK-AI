import { Card } from '@/shared/ui/Card';
import type { RepoSnapshotResponse } from '../api/useGithub';

interface RepoListProps {
  repos: RepoSnapshotResponse[];
}

export function RepoList({ repos }: RepoListProps) {
  if (repos.length === 0) {
    return (
      <p className="font-body text-sm text-text-muted">
        No synced repos yet — the first sync runs within an hour of connecting.
      </p>
    );
  }

  return (
    <div className="space-y-2">
      {repos.map((repo) => (
        <Card key={repo.id} className="flex items-center justify-between gap-4">
          <div className="min-w-0">
            <p className="truncate font-body text-sm font-medium text-text-primary">
              {repo.repoName}
            </p>
            <p className="font-body text-sm text-text-muted">
              {repo.primaryLanguage ?? 'Unknown language'} · {repo.stars} stars
            </p>
          </div>
          <p className="shrink-0 font-mono text-sm text-text-muted">
            {repo.commitsLast90Days} commits / 90d
          </p>
        </Card>
      ))}
    </div>
  );
}
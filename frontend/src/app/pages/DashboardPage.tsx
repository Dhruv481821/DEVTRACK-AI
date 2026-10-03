import { useGithubConnection, useGithubRepos } from '@/features/github/api/useGithub';
import { RepoList } from '@/features/github/components/RepoList';
import { LanguageBreakdown } from '@/features/github/components/LanguageBreakdown';

// Phase 0 exit criterion (PRD §11): "empty but fully-styled dashboard." Per the
// PRD's own product principle, empty states are a designed surface, not a
// placeholder. The GitHub section below is FR-GH-03's repo list + top-language
// breakdown — it's deliberately NOT a contribution heatmap: repo_snapshot stores
// one 90-day aggregate per repo, not day-level commit data, so a day-by-day grid
// isn't buildable from what's actually synced without a schema change (not made
// here — see GithubController's docblock).
export function DashboardPage() {
  const { data: connection } = useGithubConnection();
  const { data: repos, isLoading: reposLoading } = useGithubRepos(Boolean(connection?.connected));

  return (
    <div className="space-y-8">
      <div>
        <h1 className="mb-2 font-display text-2xl">Dashboard</h1>
        <p className="font-body text-text-muted">
          Notes, Calendar, Study Planner, DSA Tracker, and Certificates are built on the backend —
          their dashboard widgets are next. Resumes are live now; see the Resumes tab.
        </p>
      </div>

      <div>
        <h2 className="mb-3 font-display text-lg">GitHub activity</h2>

        {!connection?.connected ? (
          <p className="font-body text-sm text-text-muted">
            Connect GitHub from Settings to see your repo activity here.
          </p>
        ) : reposLoading ? (
          <p className="font-body text-sm text-text-muted">Loading repos…</p>
        ) : (
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div className="lg:col-span-2">
              <RepoList repos={repos ?? []} />
            </div>
            <LanguageBreakdown repos={repos ?? []} />
          </div>
        )}
      </div>
    </div>
  );
}
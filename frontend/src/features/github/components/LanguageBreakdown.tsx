import type { RepoSnapshotResponse } from '../api/useGithub';

interface LanguageBreakdownProps {
  repos: RepoSnapshotResponse[];
}

// FR-GH-03's "top-language breakdown" — computed here from the repo list rather
// than a dedicated backend endpoint, since the source data (primaryLanguage per
// repo) is already in hand once /github/repos has been fetched once; a second
// round-trip just to count what's already loaded would be unnecessary.
export function LanguageBreakdown({ repos }: LanguageBreakdownProps) {
  const counts = new Map<string, number>();

  for (const repo of repos) {
    const language = repo.primaryLanguage ?? 'Unknown';
    counts.set(language, (counts.get(language) ?? 0) + 1);
  }

  const entries = [...counts.entries()].sort((a, b) => b[1] - a[1]);

  if (entries.length === 0) {
    return null;
  }

  return (
    <div>
      <p className="mb-2 font-body text-sm font-medium text-text-primary">Top languages</p>
      <ul className="space-y-1">
        {entries.map(([language, count]) => (
          <li key={language} className="flex justify-between font-body text-sm text-text-muted">
            <span>{language}</span>
            <span className="font-mono">{count}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
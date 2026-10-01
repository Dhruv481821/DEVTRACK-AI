import { Card } from '@/shared/ui/Card';
import { useDsaTrends } from '../api/useDsaTrends';
import { DIFFICULTIES, DIFFICULTY_LABELS } from '../schemas/dsaProblemSchema';

export function TrendsCard() {
  const { data: trends, isLoading } = useDsaTrends();

  if (isLoading) {
    return <p className="font-body text-sm text-text-muted">Loading trends…</p>;
  }

  if (!trends || trends.totalProblems === 0) {
    return null;
  }

  const topTags = Object.entries(trends.tagFrequency)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8);

  return (
    <Card>
      <h3 className="mb-3 font-display text-lg">Trends</h3>
      <p className="mb-4 font-body text-sm text-text-muted">
        {trends.totalProblems} problem{trends.totalProblems === 1 ? '' : 's'} logged
      </p>

      <div className="mb-4">
        <p className="mb-1 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
          By difficulty
        </p>
        <div className="flex gap-4 font-body text-sm">
          {DIFFICULTIES.map((difficulty) => (
            <span key={difficulty} className="text-text-primary">
              {DIFFICULTY_LABELS[difficulty]}: {trends.difficultyDistribution[difficulty] ?? 0}
            </span>
          ))}
        </div>
      </div>

      {topTags.length > 0 && (
        <div>
          <p className="mb-1 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
            Top tags
          </p>
          <div className="flex flex-wrap gap-1.5">
            {topTags.map(([tag, count]) => (
              <span
                key={tag}
                className="rounded-full border border-border px-2 py-0.5 font-body text-xs text-text-primary"
              >
                {tag} · {count}
              </span>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
}
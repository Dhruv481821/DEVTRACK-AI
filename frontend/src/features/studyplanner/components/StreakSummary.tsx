import { useStudyStreak } from '../api/useStudyPlans';

export function StreakSummary() {
  const { data: streak, isError } = useStudyStreak();

  if (isError || !streak) return null;

  return (
    <dl className="flex flex-wrap gap-8">
      <div>
        <dt className="font-body text-xs text-text-muted">Current streak</dt>
        <dd className="font-display text-2xl text-text-primary">
          {streak.currentStreak} {streak.currentStreak === 1 ? 'day' : 'days'}
        </dd>
      </div>
      <div>
        <dt className="font-body text-xs text-text-muted">Longest streak</dt>
        <dd className="font-display text-2xl text-text-primary">
          {streak.longestStreak} {streak.longestStreak === 1 ? 'day' : 'days'}
        </dd>
      </div>
      {streak.lastActivityDate && (
        <div>
          <dt className="font-body text-xs text-text-muted">Last studied</dt>
          <dd className="font-body text-sm text-text-primary">{streak.lastActivityDate}</dd>
        </div>
      )}
    </dl>
  );
}

import { useDsaAttempts } from '../api/useDsaAttempts';

export function AttemptList({ problemId }: { problemId: string }) {
  const { data: attempts, isLoading } = useDsaAttempts(problemId, true);

  if (isLoading) {
    return <p className="font-body text-sm text-text-muted">Loading attempts…</p>;
  }

  if (!attempts || attempts.length === 0) {
    return <p className="font-body text-sm text-text-muted">No attempts logged yet.</p>;
  }

  return (
    <ul className="space-y-1.5">
      {attempts.map((attempt) => (
        <li key={attempt.id} className="font-body text-sm text-text-primary">
          <span className="text-text-muted">{attempt.attemptedAt}</span>
          {attempt.timeTakenMinutes != null && ` · ${attempt.timeTakenMinutes} min`}
          {attempt.notes && ` — ${attempt.notes}`}
        </li>
      ))}
    </ul>
  );
}
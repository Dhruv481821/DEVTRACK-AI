import { Card } from '@/shared/ui/Card';
import { useDsaProblems } from '../api/useDsaProblems';
import { CreateProblemForm } from './CreateProblemForm';
import { TrendsCard } from './TrendsCard';
import { ProblemRow } from './ProblemRow';

export function ProblemList() {
  const { data: problems, isLoading } = useDsaProblems();

  return (
    <div className="space-y-6">
      <Card>
        <CreateProblemForm />
      </Card>

      <TrendsCard />

      {isLoading && <p className="font-body text-sm text-text-muted">Loading problems…</p>}

      {!isLoading && problems?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No problems logged yet — add one above to get started.
        </p>
      )}

      <div className="space-y-3">
        {problems?.map((problem) => (
          <ProblemRow key={problem.id} problem={problem} />
        ))}
      </div>
    </div>
  );
}
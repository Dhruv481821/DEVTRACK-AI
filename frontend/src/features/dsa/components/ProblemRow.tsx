import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { AttemptList } from './AttemptList';
import { AttemptForm } from './AttemptForm';
import { DIFFICULTY_LABELS, type Difficulty } from '../schemas/dsaProblemSchema';
import type { DsaProblemResponse } from '../api/useDsaProblems';

const DIFFICULTY_COLOR: Record<Difficulty, string> = {
  EASY: 'text-success',
  MEDIUM: 'text-warning',
  HARD: 'text-danger',
};

export function ProblemRow({ problem }: { problem: DsaProblemResponse }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <Card>
      <button
        type="button"
        onClick={() => setExpanded((prev) => !prev)}
        className="flex w-full items-center justify-between text-left"
      >
        <div>
          <p className="font-body text-sm font-medium text-text-primary">{problem.title}</p>
          <div className="mt-1 flex flex-wrap items-center gap-2">
            <span className={`font-body text-xs ${DIFFICULTY_COLOR[problem.difficulty]}`}>
              {DIFFICULTY_LABELS[problem.difficulty]}
            </span>
            {problem.tags.map((tag) => (
              <span
                key={tag}
                className="rounded-full border border-border px-2 py-0.5 font-body text-xs text-text-muted"
              >
                {tag}
              </span>
            ))}
          </div>
        </div>
        <span className="font-body text-sm text-text-muted">{expanded ? '−' : '+'}</span>
      </button>

      {expanded && (
        <div className="mt-4 space-y-4 border-t border-border pt-4">
          <AttemptList problemId={problem.id} />
          <AttemptForm problemId={problem.id} />
        </div>
      )}
    </Card>
  );
}
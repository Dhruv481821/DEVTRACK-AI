import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { useDeleteStudyPlan, useStudyPlans } from '../api/useStudyPlans';
import { StudyTaskList } from './StudyTaskList';

export function StudyPlanList() {
  const { data: plans, isLoading, isError } = useStudyPlans();
  const deleteMutation = useDeleteStudyPlan();

  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  return (
    <div className="space-y-3">
      {isLoading && <p className="font-body text-sm text-text-muted">Loading plans…</p>}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load your study plans. Please try again.
        </p>
      )}

      {!isLoading && !isError && plans?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No study plans yet — add one above.
        </p>
      )}

      {plans?.map((plan) => {
        const expanded = expandedId === plan.id;
        const percent =
          plan.totalTasks > 0 ? Math.round((plan.completedTasks / plan.totalTasks) * 100) : 0;

        return (
          <Card key={plan.id}>
            <div className="flex items-start justify-between gap-4">
              <div className="min-w-0 flex-1">
                <p className="truncate font-body text-sm font-medium text-text-primary">
                  {plan.title}
                </p>
                <p className="font-body text-xs text-text-muted">
                  {plan.completedTasks}/{plan.totalTasks} tasks done
                  {plan.targetDate ? ` · target ${plan.targetDate}` : ''}
                </p>
                <div
                  className="mt-2 h-1.5 w-full max-w-xs overflow-hidden rounded-full bg-surface-raised"
                  role="progressbar"
                  aria-label={`${plan.title} progress`}
                  aria-valuemin={0}
                  aria-valuemax={100}
                  aria-valuenow={percent}
                >
                  <div className="h-full bg-signal" style={{ width: `${percent}%` }} />
                </div>
              </div>

              {confirmingId === plan.id ? (
                <div className="flex shrink-0 items-center gap-2">
                  <span className="font-body text-sm text-text-muted">Delete?</span>
                  <Button
                    type="button"
                    className="bg-danger"
                    loading={deleteMutation.isPending}
                    onClick={() =>
                      deleteMutation.mutate(plan.id, {
                        onSettled: () => setConfirmingId(null),
                      })
                    }
                  >
                    Confirm
                  </Button>
                  <Button
                    type="button"
                    className="bg-surface-raised text-text-muted"
                    onClick={() => setConfirmingId(null)}
                  >
                    Cancel
                  </Button>
                </div>
              ) : (
                <div className="flex shrink-0 items-center gap-3">
                  <button
                    type="button"
                    aria-expanded={expanded}
                    onClick={() => setExpandedId(expanded ? null : plan.id)}
                    className="font-body text-sm text-text-muted hover:text-text-primary"
                  >
                    {expanded ? 'Hide tasks' : 'Tasks'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setConfirmingId(plan.id)}
                    className="font-body text-sm text-text-muted hover:text-danger"
                  >
                    Delete
                  </button>
                </div>
              )}
            </div>

            {expanded && <StudyTaskList planId={plan.id} />}
          </Card>
        );
      })}
    </div>
  );
}

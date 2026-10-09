import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import {
  useCreateInterviewRound,
  useInterviewRounds,
  useUpdateInterviewRound,
  type InterviewOutcome,
  type InterviewRoundResponse,
} from '../api/useInterviewRounds';
import {
  createInterviewRoundSchema,
  type CreateInterviewRoundFormValues,
} from '../schemas/jobTrackerSchemas';

const OUTCOMES: { value: InterviewOutcome; label: string }[] = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'PASSED', label: 'Passed' },
  { value: 'FAILED', label: 'Failed' },
];

function RoundRow({
  applicationId,
  round,
}: {
  applicationId: string;
  round: InterviewRoundResponse;
}) {
  const updateMutation = useUpdateInterviewRound(applicationId, round.id);

  return (
    <li className="flex items-center justify-between gap-3">
      <div className="min-w-0">
        <p className="truncate font-body text-sm text-text-primary">
          {round.roundName}
        </p>
        {round.occurredAt && (
          <p className="font-body text-xs text-text-muted">
            {new Date(round.occurredAt).toLocaleDateString()}
          </p>
        )}
        {round.notes && (
          <p className="font-body text-xs text-text-muted">{round.notes}</p>
        )}
      </div>

      <Select
        aria-label={`Outcome for ${round.roundName}`}
        className="w-28 shrink-0"
        value={round.outcome}
        disabled={updateMutation.isPending}
        onChange={(e) =>
          updateMutation.mutate({ outcome: e.target.value as InterviewOutcome })
        }
      >
        {OUTCOMES.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </Select>
    </li>
  );
}

export function InterviewRoundList({ applicationId }: { applicationId: string }) {
  const { data: rounds, isLoading, isError } = useInterviewRounds(applicationId, true);
  const createMutation = useCreateInterviewRound(applicationId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateInterviewRoundFormValues>({
    resolver: zodResolver(createInterviewRoundSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(
      {
        roundName: values.roundName,
        // Backend field is an Instant; a date-only input becomes midnight UTC.
        occurredAt: values.occurredAt ? `${values.occurredAt}T00:00:00Z` : undefined,
        notes: values.notes,
      },
      { onSuccess: () => reset() },
    );
  });

  return (
    <div className="mt-4 space-y-4 border-t border-border pt-4">
      {isLoading && (
        <p className="font-body text-sm text-text-muted">Loading rounds…</p>
      )}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load interview rounds. Please try again.
        </p>
      )}

      {!isLoading && !isError && rounds?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No interview rounds logged yet.
        </p>
      )}

      {rounds && rounds.length > 0 && (
        <ul className="space-y-3">
          {rounds.map((round) => (
            <RoundRow key={round.id} applicationId={applicationId} round={round} />
          ))}
        </ul>
      )}

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-3">
        <div>
          <Label htmlFor={`round-name-${applicationId}`}>Round</Label>
          <Input
            id={`round-name-${applicationId}`}
            placeholder="e.g. Phone screen"
            {...register('roundName')}
          />
          <FieldError message={errors.roundName?.message} />
        </div>

        <div>
          <Label htmlFor={`round-date-${applicationId}`}>Date</Label>
          <Input
            id={`round-date-${applicationId}`}
            type="date"
            {...register('occurredAt')}
          />
        </div>

        <div>
          <Label htmlFor={`round-notes-${applicationId}`}>Notes</Label>
          <Input id={`round-notes-${applicationId}`} {...register('notes')} />
        </div>

        <div>
          <Button type="submit" loading={createMutation.isPending}>
            Log round
          </Button>

          {createMutation.isError && (
            <p className="mt-2 font-body text-sm text-danger">
              Couldn&apos;t log that round. Please try again.
            </p>
          )}
        </div>
      </form>
    </div>
  );
}
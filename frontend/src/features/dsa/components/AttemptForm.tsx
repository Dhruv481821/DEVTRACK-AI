import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import { useLogDsaAttempt } from '../api/useDsaAttempts';
import {
  createDsaAttemptSchema,
  attemptFormToRequest,
  type CreateDsaAttemptFormValues,
} from '../schemas/dsaAttemptSchema';

export function AttemptForm({ problemId }: { problemId: string }) {
  const logMutation = useLogDsaAttempt(problemId);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateDsaAttemptFormValues>({ resolver: zodResolver(createDsaAttemptSchema) });

  const onSubmit = handleSubmit((values) => {
    logMutation.mutate(attemptFormToRequest(values), { onSuccess: () => reset() });
  });

  return (
    <form onSubmit={onSubmit} className="space-y-3">
      <div className="flex gap-3">
        <div className="flex-1">
          <Label htmlFor={`attempt-date-${problemId}`}>Date</Label>
          <Input id={`attempt-date-${problemId}`} type="date" {...register('attemptedAt')} />
          <FieldError message={errors.attemptedAt?.message} />
        </div>
        <div className="flex-1">
          <Label htmlFor={`attempt-minutes-${problemId}`}>Time taken (minutes)</Label>
          <Input
            id={`attempt-minutes-${problemId}`}
            type="number"
            min={0}
            {...register('timeTakenMinutes')}
          />
          <FieldError message={errors.timeTakenMinutes?.message} />
        </div>
      </div>
      <div>
        <Label htmlFor={`attempt-notes-${problemId}`}>Notes (optional)</Label>
        <Textarea id={`attempt-notes-${problemId}`} rows={2} {...register('notes')} />
        <FieldError message={errors.notes?.message} />
      </div>
      <Button type="submit" loading={logMutation.isPending}>
        Log attempt
      </Button>
    </form>
  );
}
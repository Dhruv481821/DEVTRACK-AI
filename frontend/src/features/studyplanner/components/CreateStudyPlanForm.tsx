import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useCreateStudyPlan } from '../api/useStudyPlans';
import {
  createStudyPlanSchema,
  type CreateStudyPlanFormValues,
} from '../schemas/studyPlannerSchemas';

export function CreateStudyPlanForm() {
  const createMutation = useCreateStudyPlan();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateStudyPlanFormValues>({
    resolver: zodResolver(createStudyPlanSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <Label htmlFor="new-plan-title">Plan</Label>
        <Input
          id="new-plan-title"
          placeholder="e.g. System design in 6 weeks"
          {...register('title')}
        />
        <FieldError message={errors.title?.message} />
      </div>

      <div>
        <Label htmlFor="new-plan-target">Target date</Label>
        <Input id="new-plan-target" type="date" {...register('targetDate')} />
        <p className="mt-1.5 font-body text-xs text-text-muted">
          Adds the deadline to your calendar.
        </p>
      </div>

      <div className="sm:col-span-2">
        <Button type="submit" loading={createMutation.isPending}>
          Add plan
        </Button>
        {createMutation.isError && (
          <p className="mt-2 font-body text-sm text-danger">
            Couldn&apos;t add that plan. Please try again.
          </p>
        )}
      </div>
    </form>
  );
}

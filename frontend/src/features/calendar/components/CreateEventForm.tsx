import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useCreateEvent } from '../api/useEvents';
import { localToIso } from '../lib/eventTime';
import { createEventSchema, type CreateEventFormValues } from '../schemas/eventSchemas';

export function CreateEventForm() {
  const createMutation = useCreateEvent();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateEventFormValues>({
    resolver: zodResolver(createEventSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(
      {
        title: values.title,
        description: values.description,
        startAt: localToIso(values.date, values.startTime),
        endAt: values.endTime ? localToIso(values.date, values.endTime) : undefined,
      },
      { onSuccess: () => reset() },
    );
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div className="sm:col-span-2">
        <Label htmlFor="new-event-title">Title</Label>
        <Input
          id="new-event-title"
          placeholder="e.g. Mock interview"
          {...register('title')}
        />
        <FieldError message={errors.title?.message} />
      </div>

      <div>
        <Label htmlFor="new-event-date">Date</Label>
        <Input id="new-event-date" type="date" {...register('date')} />
        <FieldError message={errors.date?.message} />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <Label htmlFor="new-event-start">Start</Label>
          <Input id="new-event-start" type="time" {...register('startTime')} />
        </div>
        <div>
          <Label htmlFor="new-event-end">End</Label>
          <Input id="new-event-end" type="time" {...register('endTime')} />
        </div>
        <div className="col-span-2">
          <FieldError message={errors.endTime?.message} />
        </div>
      </div>

      <div className="sm:col-span-2">
        <Label htmlFor="new-event-description">Description</Label>
        <Input id="new-event-description" {...register('description')} />
        <FieldError message={errors.description?.message} />
      </div>

      <div className="sm:col-span-2">
        <Button type="submit" loading={createMutation.isPending}>
          Add event
        </Button>
        {createMutation.isError && (
          <p className="mt-2 font-body text-sm text-danger">
            Couldn&apos;t add that event. Please try again.
          </p>
        )}
      </div>
    </form>
  );
}

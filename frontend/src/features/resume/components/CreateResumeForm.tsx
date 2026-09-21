import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useCreateResume } from '../api/useResumes';
import { createResumeSchema, type CreateResumeFormValues } from '../schemas/resumeSchemas';

export function CreateResumeForm() {
  const createMutation = useCreateResume();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateResumeFormValues>({ resolver: zodResolver(createResumeSchema) });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <form onSubmit={onSubmit} className="flex items-end gap-3">
      <div className="flex-1">
        <Label htmlFor="new-resume-title">New resume title</Label>
        <Input id="new-resume-title" placeholder="e.g. Backend Engineer Resume" {...register('title')} />
        <FieldError message={errors.title?.message} />
      </div>
      <Button type="submit" loading={createMutation.isPending}>
        Create
      </Button>
    </form>
  );
}
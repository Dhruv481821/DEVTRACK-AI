import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import { useCreateJobApplication } from '../api/useJobApplications';
import {
  createJobApplicationSchema,
  type CreateJobApplicationFormValues,
} from '../schemas/jobTrackerSchemas';

export function CreateJobApplicationForm() {
  const createMutation = useCreateJobApplication();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateJobApplicationFormValues>({
    resolver: zodResolver(createJobApplicationSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <Label htmlFor="new-job-company">Company</Label>
        <Input
          id="new-job-company"
          placeholder="e.g. Acme Corp"
          {...register('companyName')}
        />
        <FieldError message={errors.companyName?.message} />
      </div>

      <div>
        <Label htmlFor="new-job-role">Role</Label>
        <Input
          id="new-job-role"
          placeholder="e.g. Backend Engineer"
          {...register('roleTitle')}
        />
        <FieldError message={errors.roleTitle?.message} />
      </div>

      <div className="sm:col-span-2">
        <Label htmlFor="new-job-url">Job posting URL</Label>
        <Input id="new-job-url" placeholder="https://..." {...register('jobUrl')} />
        <FieldError message={errors.jobUrl?.message} />
      </div>

      <div className="sm:col-span-2">
        <Label htmlFor="new-job-notes">Notes</Label>
        <Textarea id="new-job-notes" rows={2} {...register('notes')} />
        <FieldError message={errors.notes?.message} />
      </div>

      <div className="sm:col-span-2">
        <Button type="submit" loading={createMutation.isPending}>
          Add application
        </Button>
      </div>
    </form>
  );
}
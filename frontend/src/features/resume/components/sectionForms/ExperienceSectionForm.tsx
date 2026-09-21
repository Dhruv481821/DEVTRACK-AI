import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import {
  experienceSectionSchema,
  experienceContentToForm,
  experienceFormToContent,
  type ExperienceSectionFormValues,
} from '../../schemas/sectionSchemas';

interface Props {
  initialContent?: Record<string, unknown>;
  onSubmit: (content: Record<string, unknown>) => void;
  onCancel: () => void;
  submitting?: boolean;
}

export function ExperienceSectionForm({ initialContent, onSubmit, onCancel, submitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ExperienceSectionFormValues>({
    resolver: zodResolver(experienceSectionSchema),
    defaultValues: experienceContentToForm(initialContent ?? {}),
  });

  return (
    <form onSubmit={handleSubmit((values) => onSubmit(experienceFormToContent(values)))} className="space-y-3">
      <div>
        <Label htmlFor="exp-company">Company</Label>
        <Input id="exp-company" {...register('company')} />
        <FieldError message={errors.company?.message} />
      </div>
      <div>
        <Label htmlFor="exp-role">Role</Label>
        <Input id="exp-role" {...register('role')} />
        <FieldError message={errors.role?.message} />
      </div>
      <div className="flex gap-3">
        <div className="flex-1">
          <Label htmlFor="exp-start">Start date</Label>
          <Input id="exp-start" type="month" {...register('startDate')} />
          <FieldError message={errors.startDate?.message} />
        </div>
        <div className="flex-1">
          <Label htmlFor="exp-end">End date (blank = present)</Label>
          <Input id="exp-end" type="month" {...register('endDate')} />
        </div>
      </div>
      <div>
        <Label htmlFor="exp-bullets">Bullets (one per line)</Label>
        <Textarea id="exp-bullets" rows={4} {...register('bulletsText')} />
      </div>
      <div className="flex gap-2">
        <Button type="submit" loading={submitting}>
          Save
        </Button>
        <Button type="button" className="bg-surface-raised text-text-muted" onClick={onCancel}>
          Cancel
        </Button>
      </div>
    </form>
  );
}
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import {
  educationSectionSchema,
  educationContentToForm,
  educationFormToContent,
  type EducationSectionFormValues,
} from '../../schemas/sectionSchemas';

interface Props {
  initialContent?: Record<string, unknown>;
  onSubmit: (content: Record<string, unknown>) => void;
  onCancel: () => void;
  submitting?: boolean;
}

export function EducationSectionForm({ initialContent, onSubmit, onCancel, submitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<EducationSectionFormValues>({
    resolver: zodResolver(educationSectionSchema),
    defaultValues: educationContentToForm(initialContent ?? {}),
  });

  return (
    <form onSubmit={handleSubmit((values) => onSubmit(educationFormToContent(values)))} className="space-y-3">
      <div>
        <Label htmlFor="edu-institution">Institution</Label>
        <Input id="edu-institution" {...register('institution')} />
        <FieldError message={errors.institution?.message} />
      </div>
      <div>
        <Label htmlFor="edu-degree">Degree</Label>
        <Input id="edu-degree" {...register('degree')} />
        <FieldError message={errors.degree?.message} />
      </div>
      <div className="flex gap-3">
        <div className="flex-1">
          <Label htmlFor="edu-start">Start date</Label>
          <Input id="edu-start" type="month" {...register('startDate')} />
          <FieldError message={errors.startDate?.message} />
        </div>
        <div className="flex-1">
          <Label htmlFor="edu-end">End date (blank = present)</Label>
          <Input id="edu-end" type="month" {...register('endDate')} />
        </div>
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
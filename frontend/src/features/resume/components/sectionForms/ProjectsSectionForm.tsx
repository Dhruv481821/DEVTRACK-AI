import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import {
  projectsSectionSchema,
  projectsContentToForm,
  projectsFormToContent,
  type ProjectsSectionFormValues,
} from '../../schemas/sectionSchemas';

interface Props {
  initialContent?: Record<string, unknown>;
  onSubmit: (content: Record<string, unknown>) => void;
  onCancel: () => void;
  submitting?: boolean;
}

export function ProjectsSectionForm({ initialContent, onSubmit, onCancel, submitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ProjectsSectionFormValues>({
    resolver: zodResolver(projectsSectionSchema),
    defaultValues: projectsContentToForm(initialContent ?? {}),
  });

  return (
    <form onSubmit={handleSubmit((values) => onSubmit(projectsFormToContent(values)))} className="space-y-3">
      <div>
        <Label htmlFor="proj-name">Project name</Label>
        <Input id="proj-name" {...register('name')} />
        <FieldError message={errors.name?.message} />
      </div>
      <div>
        <Label htmlFor="proj-description">Description</Label>
        <Textarea id="proj-description" rows={3} {...register('description')} />
      </div>
      <div>
        <Label htmlFor="proj-link">Link (optional)</Label>
        <Input id="proj-link" placeholder="https://…" {...register('link')} />
        <FieldError message={errors.link?.message} />
      </div>
      <div>
        <Label htmlFor="proj-bullets">Bullets (one per line)</Label>
        <Textarea id="proj-bullets" rows={4} {...register('bulletsText')} />
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
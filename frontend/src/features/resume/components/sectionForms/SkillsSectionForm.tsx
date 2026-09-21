import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import {
  skillsSectionSchema,
  skillsContentToForm,
  skillsFormToContent,
  type SkillsSectionFormValues,
} from '../../schemas/sectionSchemas';

interface Props {
  initialContent?: Record<string, unknown>;
  onSubmit: (content: Record<string, unknown>) => void;
  onCancel: () => void;
  submitting?: boolean;
}

export function SkillsSectionForm({ initialContent, onSubmit, onCancel, submitting }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SkillsSectionFormValues>({
    resolver: zodResolver(skillsSectionSchema),
    defaultValues: skillsContentToForm(initialContent ?? {}),
  });

  return (
    <form onSubmit={handleSubmit((values) => onSubmit(skillsFormToContent(values)))} className="space-y-3">
      <div>
        <Label htmlFor="skills-text">Skills (one per line)</Label>
        <Textarea id="skills-text" rows={5} {...register('skillsText')} />
        <FieldError message={errors.skillsText?.message} />
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
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { useCreateDsaProblem } from '../api/useDsaProblems';
import {
  createDsaProblemSchema,
  tagsTextToArray,
  DIFFICULTIES,
  DIFFICULTY_LABELS,
  type CreateDsaProblemFormValues,
} from '../schemas/dsaProblemSchema';

export function CreateProblemForm() {
  const createMutation = useCreateDsaProblem();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateDsaProblemFormValues>({
    resolver: zodResolver(createDsaProblemSchema),
    defaultValues: { difficulty: 'MEDIUM' },
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(
      {
        title: values.title,
        difficulty: values.difficulty,
        tags: tagsTextToArray(values.tagsText),
      },
      { onSuccess: () => reset({ title: '', difficulty: 'MEDIUM', tagsText: '' }) },
    );
  });

  return (
    <form onSubmit={onSubmit} className="space-y-3">
      <div>
        <Label htmlFor="problem-title">Problem title</Label>
        <Input id="problem-title" placeholder="e.g. Two Sum" {...register('title')} />
        <FieldError message={errors.title?.message} />
      </div>
      <div className="flex gap-3">
        <div className="flex-1">
          <Label htmlFor="problem-difficulty">Difficulty</Label>
          <Select id="problem-difficulty" {...register('difficulty')}>
            {DIFFICULTIES.map((difficulty) => (
              <option key={difficulty} value={difficulty}>
                {DIFFICULTY_LABELS[difficulty]}
              </option>
            ))}
          </Select>
        </div>
        <div className="flex-1">
          <Label htmlFor="problem-tags">Tags (comma-separated)</Label>
          <Input id="problem-tags" placeholder="arrays, hash-map" {...register('tagsText')} />
        </div>
      </div>
      <Button type="submit" loading={createMutation.isPending}>
        Add problem
      </Button>
    </form>
  );
}
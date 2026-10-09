import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Textarea } from '@/shared/ui/Textarea';
import { useGithubConnection, useGithubRepos } from '@/features/github/api/useGithub';
import { useCreateProject } from '../api/useProjects';
import {
  createProjectSchema,
  type CreateProjectFormValues,
} from '../schemas/projectSchemas';

export function CreateProjectForm() {
  const createMutation = useCreateProject();

  // Use the existing GitHub endpoints to optionally link a repository.
  const { data: connection } = useGithubConnection();
  const { data: repos } = useGithubRepos(connection?.connected === true);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateProjectFormValues>({
    resolver: zodResolver(createProjectSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <div>
        <Label htmlFor="new-project-name">Project name</Label>
        <Input
          id="new-project-name"
          placeholder="e.g. Portfolio site"
          {...register('name')}
        />
        <FieldError message={errors.name?.message} />
      </div>

      <div>
        <Label htmlFor="new-project-repo">Linked repository</Label>
        <Select id="new-project-repo" {...register('linkedRepoSnapshotId')}>
          <option value="">None</option>
          {repos?.map((repo) => (
            <option key={repo.id} value={repo.id}>
              {repo.repoName}
            </option>
          ))}
        </Select>
      </div>

      <div className="sm:col-span-2">
        <Label htmlFor="new-project-description">Description</Label>
        <Textarea
          id="new-project-description"
          rows={2}
          {...register('description')}
        />
        <FieldError message={errors.description?.message} />
      </div>

      <div className="sm:col-span-2">
        <Button type="submit" loading={createMutation.isPending}>
          Add project
        </Button>

        {createMutation.isError && (
          <p className="mt-2 font-body text-sm text-danger">
            Couldn&apos;t add that project. Please try again.
          </p>
        )}
      </div>
    </form>
  );
}
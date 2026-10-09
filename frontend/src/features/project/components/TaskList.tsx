import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import {
  useCreateTask,
  useTasks,
  useUpdateTask,
  type TaskResponse,
  type TaskStatus,
} from '../api/useTasks';
import {
  createTaskSchema,
  type CreateTaskFormValues,
} from '../schemas/projectSchemas';

const STATUSES: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: 'To do' },
  { value: 'IN_PROGRESS', label: 'In progress' },
  { value: 'DONE', label: 'Done' },
];

function TaskRow({ projectId, task }: { projectId: string; task: TaskResponse }) {
  const updateMutation = useUpdateTask(projectId, task.id);

  return (
    <li className="flex items-center justify-between gap-3">
      <div className="min-w-0">
        <p
          className={
            task.status === 'DONE'
              ? 'truncate font-body text-sm text-text-muted line-through'
              : 'truncate font-body text-sm text-text-primary'
          }
        >
          {task.title}
        </p>
        {task.notes && (
          <p className="font-body text-xs text-text-muted">{task.notes}</p>
        )}
      </div>

      <Select
        aria-label={`Status for ${task.title}`}
        className="w-36 shrink-0"
        value={task.status}
        disabled={updateMutation.isPending}
        onChange={(e) =>
          updateMutation.mutate({ status: e.target.value as TaskStatus })
        }
      >
        {STATUSES.map((s) => (
          <option key={s.value} value={s.value}>
            {s.label}
          </option>
        ))}
      </Select>
    </li>
  );
}

export function TaskList({ projectId }: { projectId: string }) {
  const { data: tasks, isLoading, isError } = useTasks(projectId, true);
  const createMutation = useCreateTask(projectId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateTaskFormValues>({
    resolver: zodResolver(createTaskSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <div className="mt-4 space-y-4 border-t border-border pt-4">
      {isLoading && (
        <p className="font-body text-sm text-text-muted">Loading tasks…</p>
      )}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load tasks. Please try again.
        </p>
      )}

      {!isLoading && !isError && tasks?.length === 0 && (
        <p className="font-body text-sm text-text-muted">No tasks yet.</p>
      )}

      {tasks && tasks.length > 0 && (
        <ul className="space-y-3">
          {tasks.map((task) => (
            <TaskRow key={task.id} projectId={projectId} task={task} />
          ))}
        </ul>
      )}

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-3">
        <div>
          <Label htmlFor={`task-title-${projectId}`}>Task</Label>
          <Input
            id={`task-title-${projectId}`}
            placeholder="e.g. Set up CI"
            {...register('title')}
          />
          <FieldError message={errors.title?.message} />
        </div>

        <div>
          <Label htmlFor={`task-notes-${projectId}`}>Notes</Label>
          <Input id={`task-notes-${projectId}`} {...register('notes')} />
        </div>

        <div>
          <Button type="submit" loading={createMutation.isPending}>
            Add task
          </Button>

          {createMutation.isError && (
            <p className="mt-2 font-body text-sm text-danger">
              Couldn&apos;t add that task. Please try again.
            </p>
          )}
        </div>
      </form>
    </div>
  );
}
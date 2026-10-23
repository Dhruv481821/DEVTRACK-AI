import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import {
  useCreateStudyTask,
  useDeleteStudyTask,
  useStudyTasks,
  useUpdateStudyTask,
  type StudyTaskResponse,
} from '../api/useStudyPlans';
import {
  createStudyTaskSchema,
  type CreateStudyTaskFormValues,
} from '../schemas/studyPlannerSchemas';

function TaskRow({ planId, task }: { planId: string; task: StudyTaskResponse }) {
  const updateMutation = useUpdateStudyTask(planId, task.id);
  const deleteMutation = useDeleteStudyTask(planId);
  const checkboxId = `study-task-${task.id}`;

  return (
    <li className="flex items-center justify-between gap-3">
      <div className="flex min-w-0 items-center gap-3">
        <input
          id={checkboxId}
          type="checkbox"
          className="h-4 w-4 shrink-0"
          checked={task.completed}
          disabled={updateMutation.isPending}
          onChange={(e) => updateMutation.mutate({ completed: e.target.checked })}
        />
        <label htmlFor={checkboxId} className="min-w-0">
          <span
            className={
              task.completed
                ? 'block truncate font-body text-sm text-text-muted line-through'
                : 'block truncate font-body text-sm text-text-primary'
            }
          >
            {task.title}
          </span>
          {task.dueDate && (
            <span className="block font-mono text-xs text-text-muted">
              Due {task.dueDate}
            </span>
          )}
        </label>
      </div>

      <button
        type="button"
        disabled={deleteMutation.isPending}
        onClick={() => deleteMutation.mutate(task.id)}
        className="shrink-0 font-body text-sm text-text-muted hover:text-danger"
        aria-label={`Delete ${task.title}`}
      >
        Remove
      </button>
    </li>
  );
}

export function StudyTaskList({ planId }: { planId: string }) {
  const { data: tasks, isLoading, isError } = useStudyTasks(planId, true);
  const createMutation = useCreateStudyTask(planId);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateStudyTaskFormValues>({
    resolver: zodResolver(createStudyTaskSchema),
  });

  const onSubmit = handleSubmit((values) => {
    createMutation.mutate(values, { onSuccess: () => reset() });
  });

  return (
    <div className="mt-4 space-y-4 border-t border-border pt-4">
      {isLoading && <p className="font-body text-sm text-text-muted">Loading tasks…</p>}

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
            <TaskRow key={task.id} planId={planId} task={task} />
          ))}
        </ul>
      )}

      <form onSubmit={onSubmit} className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_auto_auto] sm:items-end">
        <div>
          <Label htmlFor={`study-task-title-${planId}`}>Task</Label>
          <Input
            id={`study-task-title-${planId}`}
            placeholder="e.g. Read chapter 3"
            {...register('title')}
          />
          <FieldError message={errors.title?.message} />
        </div>
        <div>
          <Label htmlFor={`study-task-due-${planId}`}>Due</Label>
          <Input id={`study-task-due-${planId}`} type="date" {...register('dueDate')} />
        </div>
        <Button type="submit" loading={createMutation.isPending}>
          Add task
        </Button>
      </form>

      {createMutation.isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t add that task. Please try again.
        </p>
      )}
    </div>
  );
}

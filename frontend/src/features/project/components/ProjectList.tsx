import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { useGithubConnection, useGithubRepos } from '@/features/github/api/useGithub';
import { useDeleteProject, useProjects } from '../api/useProjects';
import { CreateProjectForm } from './CreateProjectForm';
import { TaskList } from './TaskList';

export function ProjectList() {
  const { data: projects, isLoading, isError } = useProjects();
  const deleteMutation = useDeleteProject();

  const { data: connection } = useGithubConnection();
  const { data: repos } = useGithubRepos(connection?.connected === true);

  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  const repoNameById = new Map(repos?.map((r) => [r.id, r.repoName]));

  return (
    <div className="space-y-6">
      <Card>
        <CreateProjectForm />
      </Card>

      {isLoading && (
        <p className="font-body text-sm text-text-muted">Loading projects…</p>
      )}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load your projects. Please try again.
        </p>
      )}

      {!isLoading && !isError && projects?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No projects yet — add one above to get started.
        </p>
      )}

      <div className="space-y-3">
        {projects?.map((project) => {
          const expanded = expandedId === project.id;
          const repoName = project.linkedRepoSnapshotId
            ? repoNameById.get(project.linkedRepoSnapshotId)
            : undefined;

          return (
            <Card key={project.id}>
              <div className="flex items-center justify-between gap-4">
                <div className="min-w-0">
                  <p className="truncate font-body text-sm font-medium text-text-primary">
                    {project.name}
                  </p>
                  {project.description && (
                    <p className="font-body text-sm text-text-muted">
                      {project.description}
                    </p>
                  )}
                  {repoName && (
                    <p className="font-body text-xs text-signal">{repoName}</p>
                  )}
                </div>

                {confirmingId === project.id ? (
                  <div className="flex shrink-0 items-center gap-2">
                    <span className="font-body text-sm text-text-muted">Delete?</span>

                    <Button
                      type="button"
                      className="bg-danger"
                      loading={deleteMutation.isPending}
                      onClick={() =>
                        deleteMutation.mutate(project.id, {
                          onSettled: () => setConfirmingId(null),
                        })
                      }
                    >
                      Confirm
                    </Button>

                    <Button
                      type="button"
                      className="bg-surface-raised text-text-muted"
                      onClick={() => setConfirmingId(null)}
                    >
                      Cancel
                    </Button>
                  </div>
                ) : (
                  <div className="flex shrink-0 items-center gap-3">
                    <button
                      type="button"
                      aria-expanded={expanded}
                      onClick={() => setExpandedId(expanded ? null : project.id)}
                      className="font-body text-sm text-text-muted hover:text-text-primary"
                    >
                      {expanded ? 'Hide tasks' : 'Tasks'}
                    </button>

                    <button
                      type="button"
                      onClick={() => setConfirmingId(project.id)}
                      className="font-body text-sm text-text-muted hover:text-danger"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>

              {expanded && <TaskList projectId={project.id} />}
            </Card>
          );
        })}
      </div>
    </div>
  );
}
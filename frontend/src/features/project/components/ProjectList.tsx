import { useState } from "react";
import { Card } from "@/shared/ui/Card";
import { Button } from "@/shared/ui/Button";
import {
  useGithubConnection,
  useGithubRepos,
} from "@/features/github/api/useGithub";
import {
  useDeleteProject,
  useProjects,
  useUpdateProject,
} from "../api/useProjects";
import { CreateProjectForm } from "./CreateProjectForm";
import { TaskList } from "./TaskList";

export function ProjectList() {
  const { data: projects, isLoading, isError } = useProjects();
  const deleteMutation = useDeleteProject();
  const updateMutation = useUpdateProject();

  const { data: connection } = useGithubConnection();
  const { data: repos } = useGithubRepos(connection?.connected === true);

  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editDescription, setEditDescription] = useState("");

  const repoNameById = new Map(repos?.map((r) => [r.id, r.repoName]));

  function startEditing(project: NonNullable<typeof projects>[number]) {
    setEditingId(project.id);
    setEditName(project.name);
    setEditDescription(project.description ?? "");
    setConfirmingId(null);
  }

  function cancelEditing() {
    setEditingId(null);
    setEditName("");
    setEditDescription("");
  }

  function saveEditing(id: string) {
    const name = editName.trim();
    const description = editDescription.trim();

    if (!name) {
      window.alert("Project name is required.");
      return;
    }

    updateMutation.mutate(
      { id, name, description },
      { onSuccess: cancelEditing },
    );
  }

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

      {updateMutation.isError && (
        <p role="alert" className="font-body text-sm text-danger">
          Couldn&apos;t update the project. Please try again.
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
          const editing = editingId === project.id;
          const repoName = project.linkedRepoSnapshotId
            ? repoNameById.get(project.linkedRepoSnapshotId)
            : undefined;

          return (
            <Card key={project.id}>
              {editing ? (
                <form
                  className="space-y-3"
                  onSubmit={(event) => {
                    event.preventDefault();
                    saveEditing(project.id);
                  }}
                >
                  <label className="block font-body text-sm text-text-muted">
                    Project name
                    <input
                      className="mt-1 w-full rounded border border-border bg-surface p-2 text-text-primary"
                      value={editName}
                      onChange={(event) => setEditName(event.target.value)}
                      maxLength={200}
                      required
                    />
                  </label>

                  <label className="block font-body text-sm text-text-muted">
                    Description
                    <textarea
                      className="mt-1 w-full rounded border border-border bg-surface p-2 text-text-primary"
                      value={editDescription}
                      onChange={(event) =>
                        setEditDescription(event.target.value)
                      }
                      maxLength={2000}
                      rows={4}
                    />
                  </label>

                  <div className="flex gap-2">
                    <Button type="submit" loading={updateMutation.isPending}>
                      Save
                    </Button>
                    <Button
                      type="button"
                      className="bg-surface-raised text-text-muted"
                      onClick={cancelEditing}
                      disabled={updateMutation.isPending}
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              ) : (
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
                      <p className="font-body text-xs text-signal">
                        {repoName}
                      </p>
                    )}
                  </div>

                  {confirmingId === project.id ? (
                    <div className="flex shrink-0 items-center gap-2">
                      <span className="font-body text-sm text-text-muted">
                        Delete?
                      </span>
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
                        onClick={() => startEditing(project)}
                        className="font-body text-sm text-text-muted hover:text-text-primary"
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        aria-expanded={expanded}
                        onClick={() =>
                          setExpandedId(expanded ? null : project.id)
                        }
                        className="font-body text-sm text-text-muted hover:text-text-primary"
                      >
                        {expanded ? "Hide tasks" : "Tasks"}
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
              )}

              {expanded && !editing && <TaskList projectId={project.id} />}
            </Card>
          );
        })}
      </div>
    </div>
  );
}

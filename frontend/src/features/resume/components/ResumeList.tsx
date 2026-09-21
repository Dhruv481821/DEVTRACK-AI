import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { useDeleteResume, useResumes } from '../api/useResumes';
import { CreateResumeForm } from './CreateResumeForm';

export function ResumeList() {
  const { data: resumes, isLoading } = useResumes();
  const deleteMutation = useDeleteResume();
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  return (
    <div className="space-y-6">
      <Card>
        <CreateResumeForm />
      </Card>

      {isLoading && <p className="font-body text-sm text-text-muted">Loading resumes…</p>}

      {!isLoading && resumes?.length === 0 && (
        <p className="font-body text-sm text-text-muted">No resumes yet — create one above to get started.</p>
      )}

      <div className="space-y-3">
        {resumes?.map((resume) => (
          <Card key={resume.id} className="flex items-center justify-between">
            <Link
              to={`/resumes/${resume.id}`}
              className="font-body text-sm font-medium text-text-primary hover:text-signal"
            >
              {resume.title}
            </Link>

            {confirmingId === resume.id ? (
              <div className="flex items-center gap-2">
                <span className="font-body text-sm text-text-muted">Delete this resume?</span>
                <Button
                  type="button"
                  className="bg-danger"
                  loading={deleteMutation.isPending}
                  onClick={() => deleteMutation.mutate(resume.id, { onSettled: () => setConfirmingId(null) })}
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
              <button
                type="button"
                onClick={() => setConfirmingId(resume.id)}
                className="font-body text-sm text-text-muted hover:text-danger"
              >
                Delete
              </button>
            )}
          </Card>
        ))}
      </div>
    </div>
  );
}
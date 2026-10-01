import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import {
  useCertificates,
  useDeleteCertificate,
} from '../api/useCertificates';
import { CreateCertificateForm } from './CreateCertificateForm';
import { EditCertificateForm } from './EditCertificateForm';

export function CertificateList() {
  const {
    data: certificates,
    isLoading,
    isError,
  } = useCertificates();

  const deleteMutation = useDeleteCertificate();

  const [editingId, setEditingId] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  return (
    <div className="space-y-6">
      <Card>
        <CreateCertificateForm />
      </Card>

      {isLoading && (
        <p className="font-body text-sm text-text-muted">
          Loading certificates…
        </p>
      )}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load your certificates. Please try again.
        </p>
      )}

      {!isLoading && !isError && certificates?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No certificates yet — add one above to get started.
        </p>
      )}

      <div className="space-y-3">
        {certificates?.map((certificate) =>
          editingId === certificate.id ? (
            <Card key={certificate.id}>
              <EditCertificateForm
                certificate={certificate}
                onDone={() => setEditingId(null)}
              />
            </Card>
          ) : (
            <Card
              key={certificate.id}
              className="flex items-center justify-between gap-4"
            >
              <div className="min-w-0">
                <p className="truncate font-body text-sm font-medium text-text-primary">
                  {certificate.name}
                </p>

                <p className="truncate font-body text-sm text-text-muted">
                  {certificate.issuingOrg}
                  {certificate.issueDate
                    ? ` · ${certificate.issueDate}`
                    : ''}
                </p>

                {certificate.verificationUrl && (
                  <a
                    href={certificate.verificationUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="font-body text-sm text-signal hover:underline"
                  >
                    Verify
                  </a>
                )}
              </div>

              {confirmingId === certificate.id ? (
                <div className="flex shrink-0 items-center gap-2">
                  <span className="font-body text-sm text-text-muted">
                    Delete?
                  </span>

                  <Button
                    type="button"
                    className="bg-danger"
                    loading={deleteMutation.isPending}
                    onClick={() =>
                      deleteMutation.mutate(certificate.id, {
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
                    onClick={() => setEditingId(certificate.id)}
                    className="font-body text-sm text-text-muted hover:text-text-primary"
                  >
                    Edit
                  </button>

                  <button
                    type="button"
                    onClick={() => setConfirmingId(certificate.id)}
                    className="font-body text-sm text-text-muted hover:text-danger"
                  >
                    Delete
                  </button>
                </div>
              )}
            </Card>
          ),
        )}
      </div>
    </div>
  );
}
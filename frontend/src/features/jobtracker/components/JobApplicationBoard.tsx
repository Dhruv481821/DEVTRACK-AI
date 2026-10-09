import { Card } from '@/shared/ui/Card';
import { useJobApplications } from '../api/useJobApplications';
import { CreateJobApplicationForm } from './CreateJobApplicationForm';
import { JobApplicationCard, STAGES } from './JobApplicationCard';

export function JobApplicationBoard() {
  const { data: applications, isLoading, isError } = useJobApplications();

  return (
    <div className="space-y-6">
      <Card>
        <CreateJobApplicationForm />
      </Card>

      {isLoading && (
        <p className="font-body text-sm text-text-muted">Loading applications…</p>
      )}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load your applications. Please try again.
        </p>
      )}

      {!isLoading && !isError && applications?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No applications yet — add one above to get started.
        </p>
      )}

      {applications && applications.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-5">
          {STAGES.map((stage) => {
            const inStage = applications.filter((a) => a.stage === stage.value);
            return (
              <section
                key={stage.value}
                aria-label={stage.label}
                className="space-y-3"
              >
                <h2 className="font-body text-sm font-medium text-text-muted">
                  {stage.label} ({inStage.length})
                </h2>
                {inStage.map((application) => (
                  <JobApplicationCard
                    key={application.id}
                    application={application}
                  />
                ))}
              </section>
            );
          })}
        </div>
      )}
    </div>
  );
}
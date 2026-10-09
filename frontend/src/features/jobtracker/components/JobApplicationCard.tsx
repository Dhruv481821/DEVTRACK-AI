import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Select } from '@/shared/ui/Select';
import {
  useUpdateJobApplication,
  type JobApplicationResponse,
  type JobApplicationStage,
} from '../api/useJobApplications';
import { InterviewRoundList } from './InterviewRoundList';

export const STAGES: { value: JobApplicationStage; label: string }[] = [
  { value: 'APPLIED', label: 'Applied' },
  { value: 'SCREENING', label: 'Screening' },
  { value: 'INTERVIEW', label: 'Interview' },
  { value: 'OFFER', label: 'Offer' },
  { value: 'REJECTED', label: 'Rejected' },
];

export function JobApplicationCard({
  application,
}: {
  application: JobApplicationResponse;
}) {
  const [expanded, setExpanded] = useState(false);
  const updateMutation = useUpdateJobApplication(application.id);

  return (
    <Card className="p-4">
      <p className="truncate font-body text-sm font-medium text-text-primary">
        {application.companyName}
      </p>
      <p className="truncate font-body text-sm text-text-muted">
        {application.roleTitle}
      </p>

      {application.jobUrl && (
        <a
          href={application.jobUrl}
          target="_blank"
          rel="noreferrer"
          className="font-body text-xs text-signal hover:underline"
        >
          Job posting
        </a>
      )}

      {application.notes && (
        <p className="mt-2 font-body text-xs text-text-muted">{application.notes}</p>
      )}

      <div className="mt-3 flex items-center gap-2">
        <Select
          aria-label={`Stage for ${application.companyName}`}
          value={application.stage}
          disabled={updateMutation.isPending}
          onChange={(e) =>
            updateMutation.mutate({ stage: e.target.value as JobApplicationStage })
          }
        >
          {STAGES.map((s) => (
            <option key={s.value} value={s.value}>
              {s.label}
            </option>
          ))}
        </Select>

        <button
          type="button"
          aria-expanded={expanded}
          onClick={() => setExpanded((v) => !v)}
          className="shrink-0 font-body text-sm text-text-muted hover:text-text-primary"
        >
          {expanded ? 'Hide' : 'Rounds'}
        </button>
      </div>

      {updateMutation.isError && (
        <p className="mt-2 font-body text-xs text-danger">
          Couldn&apos;t update the stage. Please try again.
        </p>
      )}

      {expanded && <InterviewRoundList applicationId={application.id} />}
    </Card>
  );
}
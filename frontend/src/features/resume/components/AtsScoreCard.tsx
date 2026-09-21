import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import clsx from 'clsx';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import { ApiError } from '@/shared/api-client/types';
import { useScoreResume, type AtsScoreResponse } from '../api/useAtsScore';
import { atsScoreSchema, type AtsScoreFormValues } from '../schemas/atsScoreSchema';

// Seconds-until-reset is read from the 429's Retry-After header, not a message string the
// backend hardcoded — see ApiError's docblock and 09_AI_Architecture.md's design review. This
// is the one place that turns the raw number into human-readable text.
function formatRetryAfter(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.round((seconds % 3600) / 60);
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
}

export function AtsScoreCard({ resumeId }: { resumeId: string }) {
  const scoreMutation = useScoreResume(resumeId);
  const [response, setResponse] = useState<AtsScoreResponse | null>(null);
  const [quotaMessage, setQuotaMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AtsScoreFormValues>({ resolver: zodResolver(atsScoreSchema) });

  const onSubmit = handleSubmit((values) => {
    setQuotaMessage(null);
    setResponse(null);
    scoreMutation.mutate(values.jobDescription, {
      onSuccess: (data) => setResponse(data),
      onError: (error) => {
        if (error instanceof ApiError && error.status === 429) {
          const retryAfter = error.headers.get('Retry-After');
          const seconds = retryAfter ? Number(retryAfter) : NaN;
          setQuotaMessage(
            Number.isFinite(seconds)
              ? `${error.message} Resets in ${formatRetryAfter(seconds)}.`
              : error.message,
          );
        }
      },
    });
  });

  return (
    <Card>
      <h3 className="mb-3 font-display text-lg">Score against a job description</h3>

      <form onSubmit={onSubmit} className="space-y-3">
        <div>
          <Label htmlFor="ats-job-description">Job description</Label>
          <Textarea
            id="ats-job-description"
            rows={6}
            placeholder="Paste the job description here…"
            {...register('jobDescription')}
          />
          <FieldError message={errors.jobDescription?.message} />
        </div>
        <Button type="submit" loading={scoreMutation.isPending}>
          Score my resume
        </Button>
      </form>

      {quotaMessage && <p className="mt-4 font-body text-sm text-warning">{quotaMessage}</p>}

      {response?.status === 'UNAVAILABLE' && (
        <p className="mt-4 font-body text-sm text-text-muted">{response.message}</p>
      )}

      {response?.result && (
        <div className="mt-4 space-y-3 border-t border-border pt-4">
          {response.status === 'CACHED_FALLBACK' && (
            <p className="font-body text-xs text-text-muted">
              Showing a previous result from{' '}
              {response.generatedAt ? new Date(response.generatedAt).toLocaleString() : 'earlier'}{' '}
              — we couldn't reach the AI service just now.
            </p>
          )}

          <p className="font-display text-3xl">
            {response.result.score}
            <span className="text-base text-text-muted">/100</span>
          </p>

          <p className="font-body text-sm text-text-primary">{response.result.reasoning}</p>

          {response.result.matchedKeywords.length > 0 && (
            <KeywordList
              label="Matched keywords"
              keywords={response.result.matchedKeywords}
              tone="success"
            />
          )}
          {response.result.missingKeywords.length > 0 && (
            <KeywordList
              label="Missing keywords"
              keywords={response.result.missingKeywords}
              tone="danger"
            />
          )}
          {response.result.formattingIssues.length > 0 && (
            <div>
              <p className="mb-1 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
                Formatting issues
              </p>
              <ul className="list-inside list-disc font-body text-sm text-text-primary">
                {response.result.formattingIssues.map((issue, i) => (
                  <li key={i}>{issue}</li>
                ))}
              </ul>
            </div>
          )}

          <p className="font-body text-xs text-text-muted">
            Sources: {response.result.sourcesUsed.join(', ')}
          </p>
        </div>
      )}
    </Card>
  );
}

function KeywordList({
  label,
  keywords,
  tone,
}: {
  label: string;
  keywords: string[];
  tone: 'success' | 'danger';
}) {
  return (
    <div>
      <p className="mb-1 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
        {label}
      </p>
      <div className="flex flex-wrap gap-1.5">
        {keywords.map((keyword) => (
          <span
            key={keyword}
            className={clsx(
              'rounded-full border px-2 py-0.5 font-body text-xs',
              tone === 'success' ? 'border-success text-success' : 'border-danger text-danger',
            )}
          >
            {keyword}
          </span>
        ))}
      </div>
    </div>
  );
}
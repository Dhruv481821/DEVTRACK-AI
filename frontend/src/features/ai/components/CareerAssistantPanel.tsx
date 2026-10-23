import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import { ApiError } from '@/shared/api-client/types';
import { quotaMessage } from '@/shared/lib/formatRetryAfter';
import {
  useAskCareerAssistant,
  type CareerAssistantResponse,
} from '../api/useCareerAssistant';
import {
  careerAssistantSchema,
  type CareerAssistantFormValues,
} from '../schemas/careerAssistantSchema';

// Questions that need data from several modules — what this agent is for (FR-AI-01).
const SUGGESTIONS = [
  'Am I ready to apply for backend roles?',
  'What should I focus on in the next 4 weeks?',
  'Which gaps in my profile hurt my job applications most?',
];

export function CareerAssistantPanel() {
  const askMutation = useAskCareerAssistant();
  const [response, setResponse] = useState<CareerAssistantResponse | null>(null);
  const [askedQuestion, setAskedQuestion] = useState<string | null>(null);
  const [limitMessage, setLimitMessage] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<CareerAssistantFormValues>({
    resolver: zodResolver(careerAssistantSchema),
  });

  const onSubmit = handleSubmit((values) => {
    setLimitMessage(null);
    setResponse(null);
    setAskedQuestion(values.question);
    askMutation.mutate(values.question, {
      onSuccess: (data) => setResponse(data),
      onError: (error) => {
        if (error instanceof ApiError && error.status === 429) {
          setLimitMessage(quotaMessage(error.message, error.headers.get('Retry-After')));
        }
      },
    });
  });

  const genericError =
    askMutation.isError && !(askMutation.error instanceof ApiError && askMutation.error.status === 429);

  return (
    <div className="space-y-6">
      <Card>
        <form onSubmit={onSubmit} className="space-y-3">
          <div>
            <Label htmlFor="assistant-question">Your question</Label>
            <Textarea
              id="assistant-question"
              rows={3}
              placeholder="Ask about your readiness, gaps or what to do next…"
              {...register('question')}
            />
            <FieldError message={errors.question?.message} />
          </div>

          <div className="flex flex-wrap gap-2">
            {SUGGESTIONS.map((s) => (
              <button
                key={s}
                type="button"
                onClick={() => setValue('question', s, { shouldValidate: true })}
                className="rounded-md border border-border px-2.5 py-1 font-body text-xs text-text-muted hover:border-signal hover:text-text-primary"
              >
                {s}
              </button>
            ))}
          </div>

          <Button type="submit" loading={askMutation.isPending}>
            Ask
          </Button>
        </form>
      </Card>

      {askMutation.isPending && (
        <p className="font-body text-sm text-text-muted" aria-live="polite">
          Reading your DSA, GitHub, resume, applications and study data…
        </p>
      )}

      {limitMessage && (
        <p className="font-body text-sm text-warning">
          {limitMessage}{' '}
          <Link to="/billing" className="text-signal hover:underline">
            See plans
          </Link>
        </p>
      )}

      {genericError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t reach the assistant. Please try again.
        </p>
      )}

      {response?.status === 'UNAVAILABLE' && (
        <p className="font-body text-sm text-text-muted">{response.message}</p>
      )}

      {response?.result && (
        <Card className="space-y-4" aria-live="polite">
          {askedQuestion && (
            <p className="font-body text-xs uppercase tracking-wide text-text-muted">
              {askedQuestion}
            </p>
          )}

          {response.status === 'CACHED_FALLBACK' && (
            <p className="font-body text-xs text-text-muted">
              Showing your previous answer from{' '}
              {response.generatedAt ? new Date(response.generatedAt).toLocaleString() : 'earlier'}{' '}
              — the AI service couldn&apos;t be reached just now.
            </p>
          )}

          <p className="whitespace-pre-line font-body text-sm text-text-primary">
            {response.result.answer}
          </p>

          {response.result.recommendations.length > 0 && (
            <div>
              <p className="mb-1 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
                Next steps
              </p>
              <ol className="list-inside list-decimal space-y-1 font-body text-sm text-text-primary">
                {response.result.recommendations.map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ol>
            </div>
          )}

          <p className="font-body text-xs text-text-muted">
            {response.result.basedOn.length > 0
              ? `Based on: ${response.result.basedOn.join(' · ')}`
              : 'No tracked data yet — add DSA problems, a resume or connect GitHub for a grounded answer.'}
          </p>
        </Card>
      )}
    </div>
  );
}

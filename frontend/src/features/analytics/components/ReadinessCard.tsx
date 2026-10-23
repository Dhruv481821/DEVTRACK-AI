import { Card } from '@/shared/ui/Card';
import { useReadiness } from '../api/useReadiness';
import { ReadinessTrendChart } from './ReadinessTrendChart';

export function ReadinessCard() {
  const { data, isLoading, isError } = useReadiness();

  return (
    <Card>
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <h2 className="font-display text-lg">Career readiness</h2>
        <p className="font-body text-xs text-text-muted">DSA, resume and GitHub, scored daily</p>
      </div>

      {isLoading && <p className="mt-4 font-body text-sm text-text-muted">Calculating…</p>}

      {isError && (
        <p className="mt-4 font-body text-sm text-danger">
          Couldn&apos;t load your readiness score. Please try again.
        </p>
      )}

      {data && (
        <div className="mt-4 grid grid-cols-1 gap-8 lg:grid-cols-5">
          <div className="space-y-5 lg:col-span-2">
            <p className="font-display text-5xl leading-none text-text-primary">
              {data.overall}
              <span className="ml-1 font-body text-base text-text-muted">/100</span>
            </p>

            <ul className="space-y-4">
              {data.components.map((c) => (
                <li key={c.key}>
                  <div className="flex items-baseline justify-between gap-3">
                    <span className="font-body text-sm text-text-primary">{c.label}</span>
                    <span className="font-mono text-sm text-text-primary">{c.score}</span>
                  </div>
                  <div
                    className="mt-1.5 h-1.5 overflow-hidden rounded-full bg-surface-raised"
                    role="meter"
                    aria-label={c.label}
                    aria-valuemin={0}
                    aria-valuemax={100}
                    aria-valuenow={c.score}
                  >
                    <div className="h-full rounded-full bg-signal" style={{ width: `${c.score}%` }} />
                  </div>
                  <p className="mt-1.5 font-body text-xs text-text-muted">{c.signals.join(' · ')}</p>
                  {c.nextStep && (
                    <p className="mt-1 font-body text-xs text-text-primary">→ {c.nextStep}</p>
                  )}
                </li>
              ))}
            </ul>
          </div>

          <div className="lg:col-span-3">
            <p className="mb-2 font-body text-xs font-medium uppercase tracking-wide text-text-muted">
              Last 90 days
            </p>
            {data.trend.length >= 2 ? (
              <ReadinessTrendChart points={data.trend} />
            ) : (
              <p className="font-body text-sm text-text-muted">
                Your trend line starts after a second day. DevTrack records one score per day you
                open the dashboard.
              </p>
            )}
          </div>
        </div>
      )}
    </Card>
  );
}

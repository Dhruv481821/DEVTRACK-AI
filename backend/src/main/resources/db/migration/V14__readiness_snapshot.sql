-- Phase 5, readiness analytics (FR-ANALYTICS-01). One row per user per UTC day,
-- upserted whenever the readiness view is computed, so the trend chart has real
-- history without a background job. user_id is a scalar (no cross-module FK).

create table readiness_snapshot (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null,
    snapshot_date  date not null,
    overall_score  integer not null,
    dsa_score      integer not null,
    resume_score   integer not null,
    github_score   integer not null,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

-- Doubles as the trend query's index (user's snapshots ordered by date) and the
-- ON CONFLICT target that makes the daily upsert idempotent under concurrency.
create unique index idx_readiness_snapshot_user_date on readiness_snapshot (user_id, snapshot_date);

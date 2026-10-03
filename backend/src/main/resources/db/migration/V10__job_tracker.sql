-- Phase 3, Job Tracker (FR-JOB-01 only — interview rounds, FR-INT-01, are a
-- separate slice with their own migration, not built yet).

create table job_application (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null,
    company_name   text not null,
    role_title     text not null,
    job_url        text,
    stage          text not null default 'APPLIED',
    notes          text,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    deleted_at     timestamptz
);

create index idx_job_application_user on job_application (user_id);

-- Supports the kanban view's natural query shape: "this user's applications in
-- stage X" — same reasoning as every other (user_id, <filter column>) index in
-- this schema (e.g. idx_repo_snapshot_connection, notification's user_id/read
-- index from the Phase 0 schema).
create index idx_job_application_user_stage on job_application (user_id, stage);
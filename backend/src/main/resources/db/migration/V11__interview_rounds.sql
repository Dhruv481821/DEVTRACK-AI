-- Phase 3, Interview Tracker (FR-INT-01). Child of job_application, same
-- ManyToOne/FK shape as resume_section -> resume (V9).

create table interview_round (
    id                   uuid primary key default gen_random_uuid(),
    job_application_id   uuid not null references job_application (id),
    round_name           text not null,
    occurred_at          timestamptz,
    outcome              text not null default 'PENDING',
    notes                text,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);

create index idx_interview_round_job_application on interview_round (job_application_id);
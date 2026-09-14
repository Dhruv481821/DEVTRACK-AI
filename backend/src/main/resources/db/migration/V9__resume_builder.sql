-- Phase 3, Resume Builder (FR-RESUME-01/02 only — ATS scoring/FR-ATS-01 needs the
-- AI Service Layer, not built yet, deferred to its own slice).
--
-- One-to-many with user, per the note flagged since 05_Database_Architecture.md's
-- original Phase 0 write-up: a user can have multiple resumes (one per target
-- role), since FR-ATS-01 will eventually score a SPECIFIC resume against a
-- SPECIFIC job description — a one-to-one model would contradict that.

create table resume (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null,
    title        text not null,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    deleted_at   timestamptz
);

create index idx_resume_user on resume (user_id);

-- content is jsonb, structured per section_type (form-driven editor, not
-- free-text, per FR-RESUME-01's original acceptance criterion) — a single
-- flexible jsonb column per section rather than separate strongly-typed tables
-- per section type, which would be over-engineering for v1.

create table resume_section (
    id             uuid primary key default gen_random_uuid(),
    resume_id      uuid not null references resume (id),
    section_type   text not null,
    content        jsonb not null default '{}'::jsonb,
    order_index    int not null default 0,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

create index idx_resume_section_resume
    on resume_section (resume_id, order_index);
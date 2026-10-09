-- Phase 3, Project Management (FR-PROJ-01). Task is a child of project, same
-- FK shape as interview_round -> job_application (V11).
--
-- linked_repo_snapshot_id is deliberately a plain uuid with NO foreign key:
-- repo_snapshot belongs to the github module, and modules do not reference each
-- other's tables (cross-module references are scalar ids only).

create table project (
    id                       uuid primary key default gen_random_uuid(),
    user_id                  uuid not null,
    name                     text not null,
    description              text,
    linked_repo_snapshot_id  uuid,
    created_at               timestamptz not null default now(),
    updated_at               timestamptz not null default now(),
    deleted_at               timestamptz
);

create index idx_project_user on project (user_id);

create table task (
    id          uuid primary key default gen_random_uuid(),
    project_id  uuid not null references project (id),
    title       text not null,
    status      text not null default 'TODO',
    notes       text,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now()
);

create index idx_task_project on task (project_id);
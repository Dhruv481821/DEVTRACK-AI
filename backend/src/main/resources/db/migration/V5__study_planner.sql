-- Phase 1, Study Planner module — see /docs/05_Database_Architecture.md §9.
-- study_task has no deleted_at / user_id: ownership flows through study_plan_id
-- (intra-module FK, unlike the cross-module scalar pattern used elsewhere), and
-- v1 has no task-delete endpoint (06_API_Specification.md §4.3).

create table study_plan (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null,
    title        text not null,
    target_date  date,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    deleted_at   timestamptz
);

create index idx_study_plan_user on study_plan (user_id);

create table study_task (
    id             uuid primary key default gen_random_uuid(),
    study_plan_id  uuid not null references study_plan (id),
    title          text not null,
    completed      boolean not null default false,
    due_date       date,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now()
);

create index idx_study_task_plan_completed
    on study_task (study_plan_id, completed);

-- FR-PLAN-02 — one row per user per day something happened (task created or
-- completed); streak is computed from consecutive activity_date rows, not a
-- mutable counter (05_Database_Architecture.md §9's reasoning).

create table study_activity_log (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null,
    activity_date  date not null,
    created_at     timestamptz not null default now()
);

create unique index idx_study_activity_user_date
    on study_activity_log (user_id, activity_date);
-- Phase 1, Calendar module — see /docs/05_Database_Architecture.md §9. source/
-- source_id implement FR-CAL-02 ("surfaces deadlines from other modules
-- automatically") concretely: an event row with non-null source/source_id,
-- created by an event-bus listener, never typed by the user directly.

create table event (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null,
    title        text not null,
    description  text,
    start_at     timestamptz not null,
    end_at       timestamptz,
    source       text not null default 'MANUAL',
    source_id    uuid,
    created_at   timestamptz not null default now(),
    updated_at   timestamptz not null default now(),
    deleted_at   timestamptz
);
create index idx_event_user_start on event (user_id, start_at);
-- Supports the listener's upsert-on-repeat-event logic (05_Database_Architecture.md
-- §10) — find the existing auto-generated event for a given source row, if any.
create index idx_event_source on event (source, source_id);
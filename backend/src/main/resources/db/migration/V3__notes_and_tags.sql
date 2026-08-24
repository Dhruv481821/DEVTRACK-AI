-- Phase 1, Notes module — see /docs/05_Database_Architecture.md §9 for full
-- reasoning (jsonb content for Tiptap's native document format, per-user-scoped
-- tags, scalar user_id per the cross-module boundary rule established in Phase 0).

create table note (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null,
    title       text not null,
    content     jsonb not null default '{}'::jsonb,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now(),
    deleted_at  timestamptz
);
create index idx_note_user_updated on note (user_id, updated_at desc);

create table tag (
    id       uuid primary key default gen_random_uuid(),
    user_id  uuid not null,
    name     text not null
);
create unique index idx_tag_user_name on tag (user_id, name);

create table note_tag (
    note_id uuid not null references note (id),
    tag_id  uuid not null references tag (id),
    primary key (note_id, tag_id)
);
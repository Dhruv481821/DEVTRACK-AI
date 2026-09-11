-- Phase 2, GitHub Analytics.
-- access_token_encrypted implements the AES-256-GCM
-- application-layer encryption decision from 12_Security.md §4
-- — flagged there as "a concrete Phase 2 implementation task," now due.

create table github_connection (
    id                     uuid primary key default gen_random_uuid(),
    user_id                uuid not null unique,
    github_username        text not null,
    access_token_encrypted text not null,
    connected_at           timestamptz not null default now(),
    last_synced_at         timestamptz
);

create table repo_snapshot (
    id                   uuid primary key default gen_random_uuid(),
    github_connection_id uuid not null references github_connection (id),
    repo_name            text not null,
    stars                int not null default 0,
    primary_language     text,
    commits_last_90_days int not null default 0,
    synced_at            timestamptz not null default now()
);

create index idx_repo_snapshot_connection
    on repo_snapshot (github_connection_id);
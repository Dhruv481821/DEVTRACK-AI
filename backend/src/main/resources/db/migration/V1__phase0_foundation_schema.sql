-- Phase 0 schema — see /docs/05_Database_Architecture.md §7 for the full ERD and
-- reasoning behind every decision below (UUIDv7 PKs, soft delete, hashed refresh
-- tokens, etc.). This migration is intentionally the only source of schema truth —
-- Hibernate ddl-auto is set to "validate", never "update" (application.yml).

create extension if not exists pgcrypto;

create table app_user (
    id              uuid primary key default gen_random_uuid(),
    email           text not null,
    password_hash   text,                       -- nullable: Google OAuth users have none
    auth_provider   text not null,               -- 'LOCAL' | 'GOOGLE'
    email_verified  boolean not null default false,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    deleted_at      timestamptz
);
create unique index idx_app_user_email on app_user (email);

create table refresh_token (
    id              uuid primary key default gen_random_uuid(),
    user_id         uuid not null references app_user (id),
    token_hash      text not null,
    expires_at      timestamptz not null,
    revoked_at      timestamptz,
    created_at      timestamptz not null default now()
);
create unique index idx_refresh_token_hash on refresh_token (token_hash);
create index idx_refresh_token_user_active on refresh_token (user_id, revoked_at);

create table role (
    id      uuid primary key default gen_random_uuid(),
    name    text not null
);
create unique index idx_role_name on role (name);

create table user_role (
    user_id uuid not null references app_user (id),
    role_id uuid not null references role (id),
    primary key (user_id, role_id)
);

create table user_profile (
    user_id         uuid primary key references app_user (id),
    display_name    text,
    avatar_url      text,
    bio             text,
    updated_at      timestamptz not null default now()
);

create table user_settings (
    user_id                 uuid primary key references app_user (id),
    theme                   text not null default 'dark',
    notification_prefs     jsonb not null default '{}'::jsonb,
    updated_at              timestamptz not null default now()
);

create table audit_log (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid references app_user (id),
    action      text not null,
    metadata    jsonb not null default '{}'::jsonb,
    ip_address  text,
    created_at  timestamptz not null default now()
);
create index idx_audit_log_user_recent on audit_log (user_id, created_at);

create table notification (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references app_user (id),
    type        text not null,
    payload     text,
    read        boolean not null default false,
    created_at  timestamptz not null default now()
);
create index idx_notification_user_unread on notification (user_id, read, created_at);

-- Seed the single v1 role — see 05_Database_Architecture.md §7's note: user_role
-- exists so the RBAC interface is real from day one, even with one role today.
insert into role (name) values ('USER');

-- Fixes a gap surfaced during Week 2 implementation: FR-AUTH-05 (password reset)
-- and email verification both need a time-limited, single-use token table that
-- 05_Database_Architecture.md §7's original Phase 0 schema omitted. See that
-- document's dated update note for the full explanation.

create table verification_token (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references app_user (id),
    token_hash  text not null,
    type        text not null,             -- 'PASSWORD_RESET' | 'EMAIL_VERIFICATION'
    expires_at  timestamptz not null,
    used_at     timestamptz,
    created_at  timestamptz not null default now()
);
create unique index idx_verification_token_hash on verification_token (token_hash);
create index idx_verification_token_user_type on verification_token (user_id, type);

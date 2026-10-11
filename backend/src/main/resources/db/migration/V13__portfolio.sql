-- Portfolio CMS migration.
-- One portfolio per user.
-- A portfolio remains private until explicitly published.

create table portfolio (
    user_id     uuid primary key,
    slug        text not null,
    headline    text,
    published   boolean not null default false,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now()
);

create unique index idx_portfolio_slug
    on portfolio (slug);
-- Billing (Razorpay prepaid Pro passes). user_id columns are scalars, no
-- cross-module FKs. Amounts are stored in paise (integer), never floating point.

create table payment_order (
    id                   uuid primary key default gen_random_uuid(),
    user_id              uuid not null,
    razorpay_order_id    text not null,
    plan                 text not null,              -- PRO_MONTHLY | PRO_YEARLY
    amount_paise         integer not null,
    currency             text not null,
    status               text not null default 'CREATED', -- CREATED | PAID
    razorpay_payment_id  text,
    paid_at              timestamptz,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);

create unique index idx_payment_order_razorpay_order on payment_order (razorpay_order_id);
-- A Razorpay payment can fulfil at most one order, even if verify and webhook race.
create unique index idx_payment_order_razorpay_payment on payment_order (razorpay_payment_id)
    where razorpay_payment_id is not null;
create index idx_payment_order_user on payment_order (user_id);

-- One row per user who has ever bought Pro. Pro is active while pro_until > now().
create table pro_entitlement (
    user_id     uuid primary key,
    pro_until   timestamptz not null,
    updated_at  timestamptz not null default now()
);

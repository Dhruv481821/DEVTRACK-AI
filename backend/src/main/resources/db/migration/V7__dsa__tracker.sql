-- Phase 2, DSA Tracker.
-- Split problem/attempt per the original Phase 2 inventory.
-- A problem can be revisited; each visit is its own attempt row.
-- tags is jsonb, not a normalized table.

CREATE TABLE dsa_problem (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL,
    title        TEXT NOT NULL,
    difficulty   TEXT NOT NULL,
    tags         JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dsa_problem_user
    ON dsa_problem (user_id);

CREATE TABLE dsa_attempt (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dsa_problem_id     UUID NOT NULL REFERENCES dsa_problem (id),
    attempted_at       DATE NOT NULL,
    time_taken_minutes INTEGER,
    notes              TEXT,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_dsa_attempt_problem
    ON dsa_attempt (dsa_problem_id);
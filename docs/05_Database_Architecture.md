# DevTrack AI — Database Architecture

**Status:** Draft v1.0
**Depends on:** `04_System_Architecture.md`
**Feeds into:** `06_API_Specification.md`, `07_Backend_Architecture.md`

Per your own working rule, database comes before APIs. This document covers the
decisions that apply across the whole system (engine choice, conventions, ID strategy,
caching, search, storage), then the **full detailed schema for Phase 0 only** —
consistent with the phased-build approach the rest of this blueprint follows. Later
phases' entities are listed at ERD level now (for traceability against the SRS) and get
full column-level design when their phase's build actually starts, not now.

---

## 1. Why PostgreSQL (and not MongoDB)

This is a real decision, not a default — worth stating why, since the review flagged
its absence.

| Consideration | PostgreSQL | MongoDB |
|---|---|---|
| Data shape | Highly relational — a `JobApplication` belongs to a `User`, has many `InterviewRound`s, and is joined against `Resume` and `Analytics` data constantly | Better fit for document-shaped, loosely-related data with few joins |
| Cross-module correlation (the core product bet, PRD §1) | Exactly the workload relational joins are built for — "resume claims X, DSA log shows Y" is a join-and-compare query | Would require either application-level joins or denormalization that fights the product's core feature |
| Ecosystem fit | Neon (chosen in tech stack), mature JPA/Hibernate support, full-text search built in (§5), strong constraint/transaction guarantees | Would mean a different hosting choice and losing the relational guarantees this schema actually needs |
| Team reality | Solo dev already committed to Spring Data JPA | JPA-on-Mongo exists but is a worse-supported path than JPA-on-Postgres |

**Decision: PostgreSQL.** Not close, given how relational the actual data model is —
this product's differentiator is joining data across modules, which is the one thing
a document store makes harder, not easier.

## 2. Conventions

- **Naming:** `snake_case` for tables/columns, singular table names (`user`, not
  `users`) — matches JPA entity-name defaults and avoids the classic
  singular-vs-plural bikeshed by picking one and being consistent.
- **Primary keys: UUID (v7, time-ordered), not auto-increment bigint.**
  *Why:* auto-increment IDs leak information (sequential IDs let anyone estimate total
  user count or enumerate records by guessing adjacent IDs — a real issue for a
  REST API where IDs appear in URLs). UUIDv7 keeps the index-locality performance
  benefit of sequential IDs (unlike random UUIDv4, which fragments B-tree indexes)
  while not being guessable or informative. Generated application-side or via
  Postgres 17's native `uuidv7()` if available on Neon's Postgres version — confirm
  at implementation time.
- **Audit columns on every table:** `created_at`, `updated_at` (both
  `timestamptz`, not `timestamp` — always store UTC, never naive local time).
- **Soft delete on user-generated content** (per SRS `NFR-REL-01`): `deleted_at
  timestamptz null`. Auth/session tables are hard-deleted (no reason to retain expired
  refresh tokens). Queries against soft-deletable tables filter `deleted_at IS NULL` —
  enforced via a Hibernate `@Where` clause or JPA filter, not left to every query
  author to remember by hand.
- **Foreign keys:** always declared, always indexed (Postgres does not auto-index FK
  columns — a common performance gotcha worth stating explicitly so it isn't missed).
- **Migrations: Flyway**, versioned SQL files, not Hibernate `ddl-auto: update`.
  Hibernate auto-schema-generation in production is a well-known footgun (silent,
  unreviewed schema drift) — every schema change is a reviewed, checked-in migration
  file instead.

## 3. Caching Strategy

Per `04_System_Architecture.md` §3.2, Redis is introduced in **Phase 2**. What it
caches, decided now so Phase 2 has a concrete target rather than a vague "add caching
later":

| Cached data | TTL | Why |
|---|---|---|
| GitHub sync snapshots (`FR-GH-02`) | 1 hour | Avoids re-hitting GitHub's API on every dashboard load; matches "sync on a schedule," not real-time |
| AI responses (`FR-AI-02`) | Until underlying source data changes (invalidated on write, not time-based) | A cached "readiness score" is only valid until the user logs a new DSA problem or updates their resume |
| Per-user AI rate-limit counters (review finding 3.6) | Sliding 24h window | Enforces the AI fair-use policy decided in `04_System_Architecture.md` §3.2 |
| Auth rate-limit counters (`NFR-SEC-02`) | Sliding window, short (minutes) | Brute-force/enumeration protection on login endpoints |

Postgres remains the source of truth for all of the above — Redis is a cache, never
the only copy of data that matters.

## 4. Search Strategy

**Decision: Postgres native full-text search (`tsvector` + GIN index), not a separate
search service (Elasticsearch/Algolia).**

*Why:* the search surfaces in this product — Notes (`FR-NOTES-02`), Job Tracker
filtering, DSA problem tag search — are all single-user-scoped (a user only ever
searches their own data) and modest in volume (thousands of rows per user, not
millions across the system). Postgres full-text search handles this comfortably.
Introducing a dedicated search service would be solving a scale problem this product
doesn't have, at real infrastructure cost. Revisit only if search ever needs to span
public data at real scale (e.g., searching across all public portfolios) — not a v1
concern.

## 5. Storage Strategy

- **Binary media (avatars, certificate attachments, resume-related images):
  Cloudinary.** Postgres never stores file blobs — only the Cloudinary URL/public ID.
- **Resume PDF exports:** generated on-demand server-side (not pre-generated and
  stored) for v1 — simpler, and avoids storing a file that goes stale the moment the
  underlying resume data changes. If export becomes a performance bottleneck later,
  caching the generated PDF (keyed by resume `updated_at`) is the natural next step,
  not needed now.

## 6. Partitioning & Future Scaling

Not needed at v1 scale — stated explicitly as a deferred decision, not silence. The
two tables most likely to eventually warrant time-based partitioning if this product
ever has real scale: `audit_log` and `ai_conversation` (both append-heavy, time-ordered,
queried mostly by recent-window). Noted here so it's a known future lever, not a
surprise.

## 7. Phase 0 Schema (full detail)

```mermaid
erDiagram
    APP_USER ||--o{ REFRESH_TOKEN : has
    APP_USER ||--|| USER_PROFILE : has
    APP_USER ||--|| USER_SETTINGS : has
    APP_USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned_to
    APP_USER ||--o{ AUDIT_LOG : generates
    APP_USER ||--o{ NOTIFICATION : receives

    APP_USER {
        uuid id PK
        text email UK
        text password_hash
        text auth_provider
        boolean email_verified
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }
    REFRESH_TOKEN {
        uuid id PK
        uuid user_id FK
        text token_hash UK
        timestamptz expires_at
        timestamptz revoked_at
        timestamptz created_at
    }
    ROLE {
        uuid id PK
        text name UK
    }
    USER_ROLE {
        uuid user_id FK
        uuid role_id FK
    }
    USER_PROFILE {
        uuid user_id PK_FK
        text display_name
        text avatar_url
        text bio
        timestamptz updated_at
    }
    USER_SETTINGS {
        uuid user_id PK_FK
        text theme
        jsonb notification_prefs
        timestamptz updated_at
    }
    AUDIT_LOG {
        uuid id PK
        uuid user_id FK
        text action
        jsonb metadata
        text ip_address
        timestamptz created_at
    }
    NOTIFICATION {
        uuid id PK
        uuid user_id FK
        text type
        text payload
        boolean read
        timestamptz created_at
    }
```

**Table notes:**

- `app_user.password_hash` is nullable — Google OAuth users have no password.
  `auth_provider` distinguishes `LOCAL` vs `GOOGLE` (`FR-AUTH-02`).
- `refresh_token` stores a **hash** of the token, never the raw token — if the DB is
  ever compromised, stored tokens aren't directly usable. `revoked_at` supports the
  rotation-on-use requirement (`FR-AUTH-03`) — a token is marked revoked the instant
  it's exchanged for a new one, and any later attempt to reuse it is rejected and
  logged to `audit_log`.
- `user_role` is a join table even though v1 ships with exactly one role (`USER`) —
  per `04_System_Architecture.md`, the *interface* stays extensible without building
  a role-management UI that has nothing to manage yet.
- `notification.payload` is a flat text field for v1 (not a rigid schema per
  notification type) — deliberately loose since Phase 0 has zero real producers yet
  (per the event-bus decision in `04_System_Architecture.md` §3.5/§5); tightening this
  once real notification types exist in Phase 2+ is a cheap follow-up, not a redesign.

> **Update, 2026-08-08:** Week 2 implementation of `FR-AUTH-05` (password reset) and
> email verification surfaced a real gap in the original Phase 0 schema above — neither
> flow has anywhere to store a time-limited, single-use token. Added `verification_token`
> (`user_id`, `token_hash`, `type` ['PASSWORD_RESET' | 'EMAIL_VERIFICATION'], `expires_at`,
> `used_at`), migration `V2__verification_tokens.sql`. Same hashed-at-rest pattern as
> `refresh_token` — never store the raw token. Per `18_Claude_Workflow.md` §3, this is
> logged here rather than silently patched in code only.

**Indexes (beyond PK/FK, which are indexed per §2's convention):**
- `app_user(email)` — unique index, also the primary login lookup path.
- `refresh_token(user_id, revoked_at)` — supports "find this user's active tokens."
- `audit_log(user_id, created_at)` — supports "recent activity for this user," the
  actual query shape this table serves.
- `notification(user_id, read, created_at)` — supports the unread-notifications-first
  query the notification center needs.

## 8. Entity Inventory for Later Phases (traceability only — not full schema yet)

Matches the SRS §4 functional requirements. Full column-level design happens when
each phase's implementation begins, per the phased approach — listed here so the data
model is traceable end-to-end from PRD → SRS → DB from day one.

| Phase | Entities |
|---|---|
| 1 | `note`, `tag`, `event` (calendar), `study_plan`, `study_task` |
| 2 | `github_connection`, `repo_snapshot`, `dsa_problem`, `dsa_attempt`, `certificate` |
| 3 | `resume`, `resume_section`, `resume_analysis`, `job_application`, `interview_round` |
| 4 | `project`, `task`, `portfolio_page` |
| 5 | `ai_conversation`, `ai_insight` |

One early note worth flagging now rather than in Phase 3: per PRD open question §14.4
and the review's finding 1.4, `resume` should be designed as **one-to-many with User**
(a user can have multiple resumes, e.g., one per target role) since `FR-ATS-01` scores
a resume against a *specific* job description — a single-resume-per-user model would
contradict that requirement. Flagging now so it's not accidentally modeled as
one-to-one when Phase 3 schema gets designed.

---

## Next Document

`06_API_Specification.md` — REST endpoint design for Phase 0 (auth, profile, settings),
versioning strategy, response envelope format, and error handling conventions, built
directly against this schema.

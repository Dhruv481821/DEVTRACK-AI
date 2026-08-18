# DevTrack AI — System Architecture

**Status:** Draft v1.0
**Depends on:** `02_Product_Requirements_Document.md`, `03_Software_Requirements_Specification.md`,
`PRD_Architectural_Review.md`
**Feeds into:** `05_Database_Architecture.md`, `06_API_Specification.md`, `07_Backend_Architecture.md`,
`08_Frontend_Architecture.md`, `09_AI_Architecture.md`

This document exists specifically to close the 5 "Critical" findings from the
architectural review before any code gets written. Each one gets a real decision below,
recorded ADR-style (Context → Decision → Consequences) so the reasoning survives even
if you revisit it in month 8 and forget why.

---

## 1. Architecture Style

**Decision: Modular monolith, not microservices.**

*Context:* Solo developer, 12-month timeline, 20 feature modules. Microservices would
add deployment complexity, network-boundary failure modes, and cross-service data
consistency problems — all real costs — in exchange for independent scaling benefits
that a v1 with an unproven user base doesn't need yet.

*Decision:* One Spring Boot application, organized into feature packages with hard
internal boundaries (see §4), deployed as a single Railway service. One React SPA,
deployed to Vercel.

*Consequences:* Simpler deployment, simpler local dev, one database to reason about.
The tradeoff is deliberate coupling risk if module boundaries aren't enforced in code
— mitigated by the package structure and the event-publishing pattern in §5.

## 2. High-Level Component Diagram

```
┌─────────────────────┐         ┌──────────────────────────────────┐
│   React SPA          │  HTTPS  │   Spring Boot API (Railway)       │
│   (Vercel)            │────────▶│   - REST controllers (versioned)  │
│                       │         │   - Service layer per module      │
│                       │◀────────│   - Domain event bus (in-process) │
└─────────────────────┘         └───────┬───────────┬───────┬────────┘
                                          │           │       │
                          ┌───────────────┘           │       └───────────────┐
                          ▼                            ▼                      ▼
                ┌──────────────────┐        ┌──────────────────┐   ┌──────────────────┐
                │  Neon PostgreSQL  │        │  Redis (Phase 2+) │   │  External APIs    │
                │  (pooled conn.)   │        │  cache/rate-limit  │   │  GitHub, Gemini,  │
                └──────────────────┘        └──────────────────┘   │  Cloudinary, Email │
                                                                     └──────────────────┘
```

## 3. Resolving the 5 Critical Findings

### 3.1 Email provider (finding: missing from tech stack)

**Decision: Resend.**

*Context:* `FR-AUTH-05` (password reset) and email verification both require
transactional email. This was absent from the original tech stack entirely.

*Decision:* Add **Resend** to the stack. Simple API, generous free tier, good
deliverability defaults, no SMTP config to manage. Backend calls it through a thin
`EmailService` interface (not scattered call sites) so swapping providers later is a
one-file change.

*Consequences:* One more external dependency, but a required one — this isn't optional
scope, it's a Phase 0 blocker being resolved now instead of discovered mid-build.

### 3.2 Redis timing (finding: contradicted itself — "future-ready" vs. Phase 5 needing it)

**Decision: Redis is introduced in Phase 2, not Phase 5 and not Phase 0.**

*Context:* The original PRD listed Redis as "future-ready architecture" (implying not
built for v1), but `FR-AI-02` (Phase 5) requires response caching, and the review also
flagged that `FR-GH-02` (Phase 2 GitHub sync) needs caching to avoid hammering GitHub's
API on every dashboard load (review §2.3/2.5-adjacent).

*Decision:* Stand up Redis when Phase 2 (GitHub Analytics) ships — it's needed then for
GitHub API response caching, and having it already in place means Phase 5's AI response
caching is just "use the cache that already exists," not new infrastructure under
deadline pressure.

*Consequences:* Phase 2 scope grows slightly (introducing Redis, connection config,
cache-invalidation strategy for GitHub sync data). In exchange, Phase 5 gets simpler,
and the fair-use/rate-limit mechanism for AI queries (review finding 3.6) has a natural
home in the same Redis instance (e.g., a sliding-window counter per user).

### 3.3 Neon connection pooling (finding: known Neon + HikariCP failure mode)

**Decision: Use Neon's pooled connection string (PgBouncer, transaction mode) with a
small HikariCP pool, and disable server-side prepared statement caching.**

*Context:* Neon's serverless Postgres offers both a direct connection string and a
pooled one (via PgBouncer in transaction-pooling mode). Spring Boot's default HikariCP
configuration assumes a traditional always-on Postgres and will exhaust connections
under concurrent load if pointed at the direct string with a default pool size.

*Decision:*
- Use Neon's **pooled** connection string for the application's runtime connections.
- Set HikariCP `maximumPoolSize` conservatively (start at 10, tune from observed load
  — not the HikariCP default, which assumes more headroom than Railway's likely
  instance size has anyway).
- Set `hibernate.connection.provider_disables_autocommit=false` and disable JDBC
  server-side prepared statement caching (`prepareThreshold=0` equivalent), since
  PgBouncer transaction-pooling mode doesn't support session-level prepared statements
  reliably across pooled connections — a well-documented gotcha, not a hypothetical one.
- Use Neon's **direct** connection string only for the migration tool (Flyway/Liquibase)
  at deploy time, since migrations run outside the request-pooling path.

*Consequences:* Slightly more config to get right up front, but this is exactly the
kind of thing that's cheap to configure correctly on day one and expensive to debug in
production as "mysterious connection timeout under load" three months in.

### 3.4 Prompt injection handling (finding: named as a risk, zero mitigation designed)

**Decision: Structural separation of instruction and data in every AI Service Layer
call, plus a hard constraint that AI agents are read-only.**

*Context:* User-supplied content (resume text, notes) reaches LLM calls. Left
unaddressed, this is a real prompt-injection surface — e.g., resume text containing
"ignore previous instructions and..." embedded in a field the AI later processes.

*Decision (full detail belongs in `09_AI_Architecture.md`; this is the architectural
commitment that constrains it):*
- Every AI Service Layer call structurally separates system instructions from user
  content — user content is always passed as clearly delimited data (e.g., structured
  fields or fenced/tagged blocks), never concatenated into the instruction text itself.
- The system prompt for every agent explicitly states that content within user-data
  delimiters is data to analyze, not instructions to follow.
- **Hard architectural constraint: AI agents in v1 are read/analyze-only.** None of the
  six agents (PRD §12) can take actions (no "AI books this for you," no "AI deletes
  this row") — they only read data and return analysis/text. This alone eliminates the
  most damaging class of prompt-injection outcomes, because there's no destructive
  action for an injected instruction to trigger.
- Output is treated as untrusted too: AI responses are rendered as text/structured
  data in the UI, never executed, evaluated, or used to construct further queries
  without validation.

*Consequences:* This rules out some flashier future features (an AI agent that
auto-updates your job tracker for you, for instance) — that's an intentional
trade, not an oversight. Revisit only once there's a specific, well-scoped action to
allow, with its own review.

### 3.5 Calendar / cross-module dependency (finding: FR-CAL-02 has no real integration pattern)

**Decision: In-process domain event bus (Spring's `ApplicationEventPublisher`),
introduced in Phase 0.**

*Context:* Calendar needs to surface deadlines generated by modules that don't exist
yet (Job Tracker, Phase 3). Notifications (also Phase 0) has the identical shape of
problem — a consumer with no producer yet. Building this integration ad hoc, per
module, as each one ships, was the review's flagged risk (finding 5.1/5.2).

*Decision:* Introduce a lightweight **domain event** pattern in Phase 0:
- Modules publish events (e.g., `JobApplicationDeadlineSetEvent`,
  `StudyMilestoneReachedEvent`) via Spring's built-in `ApplicationEventPublisher` —
  no external message broker, this stays in-process and synchronous-or-async as needed.
- Calendar and Notifications are **listeners**, not modules that Job Tracker or Study
  Planner call directly. Job Tracker has zero knowledge that Calendar exists.
- This also directly resolves the review's YAGNI concern about building Notifications
  early (finding 5.2/simplification 2): the *consumer* (listener plumbing) is cheap to
  build in Phase 0, and it's fine for it to sit with no events flowing until Phase 2/3
  producers exist — that's a normal, inert state for an event listener, unlike a
  half-built feature with no data.

*Consequences:* A small amount of Phase 0 infrastructure that has no visible effect
until Phase 1-3. Worth it specifically because it prevents the coupling the review
warned about — without this, Calendar would eventually need direct knowledge of Job
Tracker's and Study Planner's internals, which breaks the module-boundary principle
in §1.

## 4. Module Boundaries (backend package structure)

```
com.devtrack
├── auth/            (FR-AUTH-*, FR-AUTHZ-*)
├── profile/          (FR-PROF-*, FR-SET-*)
├── notes/             (FR-NOTES-*)
├── calendar/          (FR-CAL-* — listens to events, never called directly)
├── studyplanner/      (FR-PLAN-*)
├── github/            (FR-GH-* — owns GitHub OAuth + sync + Redis cache)
├── dsa/               (FR-DSA-*)
├── certificates/      (FR-CERT-*)
├── resume/            (FR-RESUME-*, FR-ATS-*)
├── jobtracker/        (FR-JOB-*, FR-INT-*)
├── project/           (FR-PROJ-*)
├── portfolio/         (FR-PORT-* — see §6 for public-surface handling)
├── ai/                (AI Service Layer — provider-agnostic, see §7)
├── analytics/         (FR-ANALYTICS-*)
├── notifications/     (FR-NOTIF-* — listens to events, never called directly)
├── common/
│   ├── events/        (domain event definitions + publisher wiring)
│   ├── email/          (EmailService interface + Resend adapter)
│   └── security/        (JWT, RBAC, rate limiting)
```

**Rule enforced from Phase 0:** a module may depend on `common/`, but modules do not
import each other directly except through the event bus or through the `ai/` service
layer (which is allowed to read across modules by design, since cross-module
correlation is its entire job — see §7).

## 5. Event-Driven Integration Pattern (detail on 3.5)

- Events are plain Java records under `common/events/`, e.g.:
  `JobApplicationDeadlineSetEvent(userId, jobApplicationId, deadline)`.
- Producers publish via `ApplicationEventPublisher.publishEvent(...)` — no direct
  dependency on any listener.
- Listeners (`calendar/`, `notifications/`) subscribe via `@EventListener` /
  `@Async` where appropriate (notification delivery shouldn't block the request that
  triggered it).
- This pattern is intentionally the *smallest* thing that solves the problem — no
  message broker, no Kafka, no outbox pattern. Those are real future upgrades if
  cross-module event volume or reliability needs ever justify them, not v1 needs.

## 6. Portfolio CMS — Public Surface Handling

Addresses review findings 2.3 (scaling) and 3.1 (accidental data leakage).

- **Data leakage control:** the public portfolio endpoint (`FR-PORT-02`) uses a
  dedicated, allowlist-only response DTO (`PortfolioPublicView`), never the internal
  `Project`/`Certificate`/`GithubConnection` entities or their authenticated-endpoint
  DTOs. Adding a new field to the internal Project model does **not** automatically
  expose it publicly — someone has to deliberately add it to `PortfolioPublicView`.
  This is the concrete rule the review asked for, not just a principle.
- **Scaling control:** given the frontend is a Vite SPA (not Next.js/SSR), true static
  generation isn't available without changing the frontend framework choice — which
  isn't worth doing for one feature. Accepted v1 tradeoff: the public portfolio
  endpoint sets aggressive `Cache-Control` headers (e.g., `max-age=300`,
  `stale-while-revalidate`), letting Vercel's edge cache absorb most read traffic
  without touching the backend on every request. Revisit only if a specific portfolio
  page's traffic pattern actually demands it — not a v1 concern to over-solve now.

## 7. AI Service Layer

- Single provider-agnostic interface (`AiProvider`), with a `GeminiAiProvider`
  implementation — swapping providers later touches one adapter (per SRS
  `NFR-MAINT-02`).
- Per the review's simplification recommendation (finding/opportunity 6): the six
  user-facing AI capabilities (PRD §12) are implemented as **one backend service with
  six prompt/context configurations**, not six independently-maintained agent classes.
  Each configuration defines: which modules' data it reads, its system prompt, and its
  output shape. This keeps the actual "agent count" the user experiences at six while
  keeping the code the developer maintains at one well-tested service.
- Enforces the read-only constraint from §3.4.
- Full prompt strategy, context-window budgeting (review finding 4.1), and caching
  design belongs in `09_AI_Architecture.md` — this section only fixes the parts that
  are genuinely architectural (provider abstraction, module boundary, action
  constraints).

## 8. Deployment Topology

| Component | Host | Notes |
|---|---|---|
| React SPA | Vercel | Static build, edge-cached |
| Spring Boot API | Railway | Single instance for v1 (see §3.3 scheduler note below) |
| PostgreSQL | Neon | Pooled connection string for runtime, direct for migrations |
| Redis | Introduced Phase 2 | Provider TBD in `14_DevOps.md` — likely Railway add-on or Upstash |
| Media | Cloudinary | Avatars, certificate attachments |
| Email | Resend | Transactional only (reset, verification) |

**Single-instance note:** `FR-GH-02`'s scheduled sync (review finding 2.2) is safe as
long as the API runs as a single Railway instance — Spring's `@Scheduled` will
double-fire if horizontally scaled. This is fine for v1 traffic expectations, but it's
a stated constraint, not an accident: if you ever scale to multiple instances, the sync
job needs to move to a distributed-lock-aware scheduler first.

---

## Next Document

`05_Database_Architecture.md` — schema design for Phase 0 entities first
(User, Session, RefreshToken, Role, UserProfile, UserSettings), including the
PostgreSQL-vs-alternatives justification and indexing strategy the review flagged as
missing from the PRD. Database comes before APIs, per your own rule.

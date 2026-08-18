# DevTrack AI — DevOps

**Status:** Draft v1.0
**Depends on:** `13_Testing.md`, `04_System_Architecture.md` §8
**Feeds into:** `15_Deployment.md`, `16_Git_Workflow.md`

---

## 1. Environment Strategy — Two Real Environments, Not Three

**Decision: Local Dev + Production, not a full Dev/Staging/Prod split — with Vercel's
automatic PR preview deployments standing in for "staging" on the frontend.**

*Why, stated plainly:* a real always-on staging environment means a second Railway
backend instance, a second Neon database branch kept roughly in sync, and a second
set of secrets to manage — real ongoing cost and maintenance for a project that's
explicitly been built free-tier-conscious throughout this whole doc set (Neon,
Cloudinary, Gemini, Resend all chosen for their free tiers). For a solo developer,
that overhead buys less than it costs. What actually catches problems before
production is:
- Vercel's automatic **preview deployment per PR** (frontend, free, zero extra
  config) — real, deployed frontend behavior to click through before merging.
- The CI test suite (`13_Testing.md`) gating every PR, including the Testcontainers-
  backed integration tests against real Postgres behavior — the thing a separate
  staging *database* would mostly be catching anyway.
- Neon's **branching** feature can create a throwaway DB branch off production data
  for a specific risky migration to test against, on demand, without maintaining a
  permanent second environment — used situationally, not as a standing environment.

**Revisit this decision** the moment either of two things becomes true: real paying
users exist (production incidents now have real cost), or a change is risky enough
that "test locally + PR preview + CI" genuinely isn't enough confidence. Until then,
a permanent staging environment is solving a problem this project doesn't have yet.

## 2. Local Development — Docker Compose

```yaml
# docker-compose.yml (local dev only — not used in CI, which uses Testcontainers
# per-test per 13_Testing.md §2, and not used in production, which uses managed
# Neon/Redis per 04_System_Architecture.md §8)
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: devtrack_dev
    ports: ["5432:5432"]
  redis:            # added when Phase 2 lands, per 04_System_Architecture.md §3.2
    image: redis:7
    ports: ["6379:6379"]
```

Backend and frontend run natively (Spring Boot via Maven/Gradle, Vite dev server) for
faster local iteration than containerizing them too — only the stateful dependencies
(Postgres, Redis) need containerizing locally, since they're the pieces that are
awkward to install/version-match by hand.

## 3. CI Pipeline (GitHub Actions)

**On every PR:**
1. Lint (ESLint + Prettier frontend; Checkstyle/Spotless backend) — fails fast, before
   the more expensive steps run.
2. Backend test suite (`13_Testing.md` §2) — unit + Testcontainers integration tests.
3. Frontend test suite (`13_Testing.md` §3) — Vitest + RTL, MSW-backed.
4. Build check (both frontend and backend compile/build successfully).
5. E2E suite (`13_Testing.md` §5's four journeys) — run against the PR's Vercel
   preview URL + a CI-provisioned backend instance, so this step depends on step 4's
   build artifacts.

**On merge to `main`:**
1. All of the above, plus:
2. **Flyway migration run** against Neon's **direct** connection string
   (`05_Database_Architecture.md` §3's decision — direct string reserved specifically
   for migrations, pooled string for runtime traffic), as its own explicit pipeline
   step **before** the new backend version deploys — not run automatically on
   application startup. This ordering matters: running migrations as a distinct,
   observable CI step (rather than "whichever instance boots first migrates,
   implicitly") is both safer given the single-instance constraint
   (`04_System_Architecture.md` §8) and gives a clear, loggable point of failure if a
   migration breaks, before any new application code is even deployed against it.
3. Deploy backend to Railway.
4. Frontend deploy to Vercel happens automatically on push to `main` (Vercel's own
   git integration) — no separate Actions step needed for it.

> **Update, 2026-08-17:** Real deployment surfaced two deviations from the plan
> above, worth recording rather than silently overwriting:
>
> 1. **Migrations run via Spring Boot startup (`FlywayMigrationInitializer`), not a
>    separate CI step.** During actual deployment, Railway's own native GitHub
>    integration was already auto-deploying on every push — independent of
>    `ci.yml` — and Flyway's startup migration worked correctly against Railway's
>    single-instance deployment every time. Keeping it this way rather than adding a
>    redundant explicit CI migration step: it's simpler, and the single-instance
>    constraint (`04_System_Architecture.md` §8) that made the original "explicit
>    step" ordering matter still holds regardless of which mechanism runs the
>    migration.
> 2. **The real gap this surfaced: Railway's native auto-deploy has no dependency
>    on `ci.yml`'s test suite passing.** A failing test doesn't block a bad deploy —
>    two independent triggers on the same push event, not a gate. §3's job below
>    fixes this by disabling Railway's raw auto-deploy and triggering it explicitly
>    from GitHub Actions only after the backend test job succeeds. Vercel's native
>    auto-deploy is deliberately left alone — it's lower-stakes (no database, easy
>    rollback) and its PR preview deployments are a real feature worth keeping.

## 4. Migration Safety

**Convention: migrations are additive/backward-compatible wherever feasible** — add a
new nullable column rather than altering an existing one in a breaking way; deprecate-
then-remove over two separate migrations rather than one destructive change. This
matters specifically because of §3's ordering (migrations run *before* the new backend
deploys): for a brief window, the *old* backend code is running against the
*new* schema, so a migration that the old code can't tolerate (e.g., dropping a column
it still reads) causes a real outage in that window, not a hypothetical one.

## 5. Secrets in CI/CD

GitHub Actions repository secrets hold: Railway deploy token, Neon migration
connection string, and any secret needed for the E2E step to reach a real (test)
instance of external services where relevant. **These are CI-scoped secrets, separate
from the application runtime secrets** (`12_Security.md` §9, injected directly by
Railway/Vercel) — the CI pipeline and the running application don't share a secrets
store, so a CI credential leak and a runtime credential leak are independent failure
domains, not the same one.

## 6. Rollback

- **Application code:** both Railway and Vercel support instant rollback to a
  previous deployment — the fast, cheap part of rollback.
- **Database migrations:** the real constraint, addressed by §4's additive-migration
  convention rather than by attempting automated migration rollback (Flyway doesn't
  do this safely for arbitrary migrations, and pretending otherwise is worse than
  planning around it). A migration that must be reverted is reverted by writing a new
  forward migration that undoes it — never by deleting or editing a migration file
  that's already been applied to production.

## 7. Health Checks

Spring Boot Actuator's `/actuator/health` endpoint, configured as Railway's health
check target — Railway restarts the instance automatically if it fails, and this is
also the endpoint an uptime monitor (§8) polls.

## 8. Observability Baseline (resolves the deferral from `07_Backend_Architecture.md` §7)

**Decision, sized honestly for v1 traffic: no dedicated APM tool (Datadog/New
Relic/etc.) for v1** — real cost and setup overhead not justified before there's
meaningful traffic to observe. What's actually in place:

- **Structured JSON logs** (already decided in `07_Backend_Architecture.md` §7),
  queried through Railway's built-in log viewer — sufficient for a single-instance
  app at v1 scale.
- **Correlation IDs** (also from `07_Backend_Architecture.md` §7) make a specific
  request traceable through those logs even without a dedicated tracing tool.
- **One free uptime monitor** (e.g., UptimeRobot or Better Stack's free tier) polling
  `/actuator/health` every few minutes, alerting via email/SMS if the app goes down —
  this is the one piece of "someone tells me when it's broken" infrastructure that's
  worth having from day one regardless of scale, because the alternative is a user
  discovering an outage before you do.

**Revisit this section specifically** the moment log volume or incident frequency
makes grep-through-Railway's-log-viewer genuinely painful — that's the real signal
to invest in a dedicated observability stack, not a fixed month on the roadmap.

## 9. Dependency Vulnerability Scanning

Resolves the item flagged as deferred-to-this-doc in `12_Security.md` §12: **GitHub
Dependabot enabled for both the npm (frontend) and Maven (backend) ecosystems**,
weekly schedule, auto-opens PRs for vulnerable dependency updates. Cheap to enable,
genuinely useful, no reason to delay past Phase 0.

---

## Next Document

`15_Deployment.md` — the concrete, step-by-step first deployment (Phase 0 exit
criterion from the PRD: "the deploy pipeline pushes to a live URL on merge to main")
and the environment variable checklist across Vercel/Railway/Neon/Cloudinary/Resend.

# DevTrack AI — Deployment

**Status:** Draft v1.0
**Depends on:** `14_DevOps.md`
**Feeds into:** `16_Git_Workflow.md`

This document is scoped to **Phase 0's exit criterion specifically**: "a user can
sign up, log in, see an empty but fully-styled dashboard, and the deploy pipeline
pushes to a live URL on merge to main" (PRD §11). Later phases introduce new external
services (Redis in Phase 2, GitHub OAuth in Phase 2, Gemini in Phase 5) — each adds
its own env vars to §2 **when that phase starts**, not provisioned upfront. Standing
up Phase 5's Gemini key before Phase 0 even ships is exactly the kind of
premature setup this doc set has avoided throughout — noted explicitly so future-you
doesn't front-load it out of habit.

---

## 1. First Deployment — Step by Step

1. **Provision Neon.** Create the Postgres project. Note both connection strings
   Neon provides: the **pooled** one (PgBouncer transaction mode — runtime traffic,
   `05_Database_Architecture.md` §3.3) and the **direct** one (migrations only, used
   by CI, never by the running application).
2. **Run the initial Flyway migration** (Phase 0 schema, `05_Database_Architecture.md`
   §7) locally against the direct connection string, to establish the baseline schema
   before the app ever tries to boot against it.
3. **Google Cloud Console:** create OAuth2 credentials for Google login
   (`FR-AUTH-02`) — client ID/secret, with an authorized redirect URI pointing at the
   backend's `/auth/google/callback` (the Railway URL, set after step 4).
4. **Railway:** create the project, link the GitHub repo, configure the build (Maven/
   Gradle), and set the Phase 0 backend environment variables (§2). Note the assigned
   Railway URL — needed for step 3's redirect URI and step 7's CORS config.
5. **Resend:** create an account, verify a sending domain (needed for password reset/
   verification emails, `FR-AUTH-05`, to land reliably rather than in spam), get the
   API key.
6. **Cloudinary:** create an account, get the cloud name/API key/secret for avatar
   uploads (`FR-PROF-01`).
7. **Vercel:** create the project, link the GitHub repo, confirm the Vite build
   settings, and set `VITE_API_BASE_URL` to the Railway backend URL from step 4.
8. **Close the loop:** update the backend's `CORS_ALLOWED_ORIGIN` (Railway env var)
   to the actual Vercel URL from step 7 — this has to happen after step 7, since the
   Vercel URL doesn't exist until then.
9. **GitHub Actions secrets:** set `RAILWAY_DEPLOY_TOKEN` and the Neon **direct**
   connection string (as a CI-scoped secret, per `14_DevOps.md` §5 — never the same
   credential store as the Railway runtime env).
10. **Merge to `main`.** Watch the `14_DevOps.md` §3 pipeline run: lint → tests →
    migration → backend deploy. Vercel deploys the frontend automatically on the same
    push.
11. **Smoke test against the live URL** (§4) — this is the actual verification of
    Phase 0's exit criterion, not "the pipeline went green."

## 2. Environment Variable Checklist (Phase 0)

**Railway (backend runtime):**

| Variable | Source |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | Neon **pooled** connection string (step 1) |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | Generated once at setup (RS256 keypair, `12_Security.md` §2.2) — treat as the most sensitive secret in the system |
| `RESEND_API_KEY` | Step 5 |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Step 6 |
| `GOOGLE_OAUTH_CLIENT_ID` / `GOOGLE_OAUTH_CLIENT_SECRET` | Step 3 |
| `CORS_ALLOWED_ORIGIN` | The Vercel URL (step 8) |

> **Update, 2026-08-15:** Deployment surfaced a real gap — `application-prod.yml`
> originally hardcoded `server.port: 8080`, which breaks on Railway specifically,
> since it assigns a dynamic port via its own `$PORT` variable and doesn't
> guarantee traffic actually routes to 8080. Fixed to `server.port: ${PORT:8080}`.
> No manual Railway variable needed — Railway provides `$PORT` automatically.
| `FRONTEND_BASE_URL` | Used to build links inside verification/password-reset emails |

**Vercel (frontend):**

| Variable | Source |
|---|---|
| `VITE_API_BASE_URL` | The Railway backend URL (step 4) |

**GitHub Actions secrets (CI-scoped, per `14_DevOps.md` §5 — separate store from the
above):**

| Secret | Source |
|---|---|
| `RAILWAY_DEPLOY_TOKEN` | Railway project settings |
| `NEON_MIGRATION_DB_URL` | Neon **direct** connection string (step 1) |

**Added later, not now (noted here only so they're not forgotten when their phase
starts):**
- Phase 2: `REDIS_URL`, `GITHUB_OAUTH_CLIENT_ID`/`SECRET`, `TOKEN_ENCRYPTION_KEY`
  (`12_Security.md` §4)
- Phase 5: `GEMINI_API_KEY`

## 3. Custom Domain (optional, not a Phase 0 blocker)

Both Vercel and Railway support custom domains with automatically provisioned SSL.
Not required for Phase 0's exit criterion (a working live URL, even on Vercel's/
Railway's default subdomain, satisfies it) — a nice-to-have to add whenever a real
domain is purchased, not a dependency of shipping Phase 0.

## 4. Post-Deploy Smoke Test

The actual verification of Phase 0's exit criterion, performed manually against the
live URL after every deploy to `main` (not automated as part of §1's step 10 for v1 —
a lightweight manual checklist is proportionate at this scale; automate this
specifically as a Playwright smoke test only if manual verification ever becomes a
bottleneck):

1. `GET /actuator/health` returns healthy.
2. Register a new account → verification email arrives (Resend).
3. Log in → dashboard shell renders (empty state, per `04_System_Architecture.md`'s
   Phase 0 scope).
4. Log in via Google OAuth → same dashboard, confirming
   `FR-AUTH-02`'s "downstream code never branches on auth method" guarantee actually
   holds in the deployed environment, not just in tests.
5. Update profile display name/avatar → Cloudinary upload succeeds.
6. Log out → refresh token cookie is cleared, protected routes redirect to login.

---

## Next Document

`16_Git_Workflow.md` — branch strategy, commit conventions (Conventional Commits, per
your original working rules), and how the phase-based roadmap (PRD §11) maps onto
actual branches and PRs.

# DevTrack AI — Software Requirements Specification (SRS)

**Status:** Draft v1.0
**Depends on:** `02_Product_Requirements_Document.md`
**Feeds into:** `04_System_Architecture.md`, `05_Database_Architecture.md`

---

## 1. Purpose & Scope

This document translates the PRD into requirements precise enough to design a data
model and API surface from. Every functional requirement (FR) has an ID, a phase
tag (matching PRD §6), and acceptance criteria. Every non-functional requirement (NFR)
has a measurable target — "fast" and "secure" are not requirements, they're wishes.

Convention: `FR-<MODULE>-<NUMBER>`. Phase tags reference PRD §6 (P0–P6).

## 2. Resolved Open Questions (defaults — override any of these anytime)

These were left open in the PRD. Defaults chosen for lowest-friction v1 delivery:

| # | Question | Default chosen | Reasoning |
|---|---|---|---|
| 1 | Portfolio CMS URL structure | Path-based: `devtrack.ai/u/{username}` | Subdomain-per-user needs wildcard DNS + wildcard SSL, which is extra Vercel/DNS config for zero functional benefit at v1 scale. Revisit if this ever needs white-label branding. |
| 2 | GitHub OAuth scope | Read-only (`repo:read`, `user:read`) | Write access (auto-creating issues) is a real feature idea but expands the security surface and re-auth burden for a Phase 2 module. Add as an explicit opt-in scope upgrade later, not baked into v1. |
| 3 | Resume export format | PDF only for v1; DOCX explicitly deferred to backlog | DOCX generation (via a library like `docx` or Apache POI) is a non-trivial dependency for a feature that most ATS systems accept as PDF anyway. |

If you disagree with any of these, say so before `04_System_Architecture.md` — the DB
schema and API contracts will be built against these defaults.

## 3. System Overview

DevTrack AI is a client-server SPA. React SPA (Vercel) talks to a Spring Boot REST
API (Railway) over versioned JSON endpoints, backed by PostgreSQL (Neon), Cloudinary
for media, and Google Gemini for AI features. Authentication is JWT-based with
refresh-token rotation; the frontend never talks to Gemini, GitHub, or Cloudinary
directly — everything is proxied through the backend so secrets never reach the
client (see `12_Security.md` for the full threat model, written after this doc).

---

## 4. Functional Requirements

### 4.1 Phase 0 — Foundation

**Authentication**

- `FR-AUTH-01` [P0] Users can register with email + password. Password must meet
  minimum complexity (≥8 chars, 1 upper, 1 number). *Acceptance:* weak passwords
  rejected with a specific validation message, not a generic 400.
- `FR-AUTH-02` [P0] Users can log in with email/password or Google OAuth2.
  *Acceptance:* both paths issue the same JWT/refresh-token pair shape; downstream
  code never needs to know which method was used.
- `FR-AUTH-03` [P0] Access tokens expire in ≤15 minutes; refresh tokens are rotated
  on use (old refresh token invalidated the moment a new one is issued).
  *Acceptance:* replaying an old refresh token after rotation returns 401 and
  triggers a logged security event.
- `FR-AUTH-04` [P0] Users can log out, which invalidates the current refresh token
  server-side (not just client-side token deletion).
- `FR-AUTH-05` [P0] Password reset via emailed time-limited token (≤30 min validity).

**Authorization**

- `FR-AUTHZ-01` [P0] Every API endpoint declares required role(s) even though v1
  ships with a single `USER` role — the RBAC check must exist and be enforced from
  day one so adding `ADMIN` later is a config change, not a rewrite.

**Dashboard shell**

- `FR-DASH-01` [P0] Authenticated users see a persistent nav shell (sidebar +
  command palette entry point) on every page.
- `FR-DASH-02` [P0] Dashboard home renders widget placeholders even before any
  feature module has data — empty states must be designed, not just blank.

**Profile / Settings**

- `FR-PROF-01` [P0] Users can view/edit display name, avatar (Cloudinary upload),
  and bio.
- `FR-SET-01` [P0] Users can toggle theme (dark/light), notification preferences.

**Notifications (infra only in P0)**

- `FR-NOTIF-01` [P0] In-app notification center exists and can render a
  notification created by any backend service — no producer modules exist yet,
  but the consumer/UI must be built and testable with seed data.

### 4.2 Phase 1 — Core Productivity

- `FR-NOTES-01` [P1] Users can create/edit/delete rich-text notes with tags.
- `FR-NOTES-02` [P1] Notes are searchable by title, content, and tag.
- `FR-CAL-01` [P1] Users can create calendar events manually.
- `FR-CAL-02` [P1] Calendar surfaces deadlines from other modules automatically
  once those modules exist (Job Tracker deadlines, Study Planner milestones) —
  *this requirement has a dependency forward to Phase 2/3 and should be designed
  as an event-subscription pattern now, not hardcoded per-module later.*
- `FR-PLAN-01` [P1] Users can define a study plan with goals and target dates.
- `FR-PLAN-02` [P1] Study Planner tracks daily/weekly streaks.

### 4.3 Phase 2 — Developer Signal

- `FR-GH-01` [P2] Users can connect a GitHub account via OAuth (read-only scope,
  per §2 default).
- `FR-GH-02` [P2] System syncs contribution activity and repo metadata on a
  schedule (not on every page load — rate-limit conscious per PRD §8 risk).
- `FR-GH-03` [P2] Dashboard renders a contribution heatmap and top-language
  breakdown from synced data.
- `FR-DSA-01` [P2] Users can log solved problems (title, difficulty, tags, date,
  time taken, notes).
- `FR-DSA-02` [P2] System computes trend views: problems/week, difficulty
  distribution, tag coverage gaps.
- `FR-CERT-01` [P2] Users can upload/list certificates with issuing org, date,
  and verification URL.

### 4.4 Phase 3 — Career Toolkit

- `FR-RESUME-01` [P3] Users can build a structured resume (sections: experience,
  education, projects, skills) via a form-driven editor, not free-text.
- `FR-RESUME-02` [P3] Resume exports to PDF (per §2 default).
- `FR-ATS-01` [P3] System scores a resume against a target job description:
  keyword coverage, formatting issues, quantified-impact check.
  *Acceptance:* score is explainable — user sees *why* the score is what it is,
  not just a number.
- `FR-JOB-01` [P3] Users can track job applications through stages (Applied →
  Screening → Interview → Offer/Rejected) in a kanban view.
- `FR-INT-01` [P3] Users can log interview rounds per application with notes and
  outcomes.

### 4.5 Phase 4 — Build & Show

- `FR-PROJ-01` [P4] Users can create project boards with tasks; a project can
  optionally link to a synced GitHub repo.
- `FR-PORT-01` [P4] Users can generate a public portfolio page composed from
  their real tracked data (projects, GitHub stats, certificates) — not
  re-typed content.
- `FR-PORT-02` [P4] Portfolio page is served at `/u/{username}` (per §2 default)
  and is publicly accessible without auth.

### 4.6 Phase 5 — AI Integration

- `FR-AI-01` [P5] AI Assistant can answer questions that require joining data
  across ≥2 modules (e.g., "am I ready to apply for backend roles?" pulls DSA
  Tracker + Resume Analyzer + GitHub Analytics).
  *Acceptance criterion for "done":* this is the one requirement in the whole
  doc I'd call a hard gate — if the assistant can only answer single-module
  questions, Phase 5 is not complete, regardless of how polished the chat UI is.
- `FR-AI-02` [P5] AI responses are cached where the underlying data hasn't
  changed, to respect Gemini free-tier limits (PRD §8 risk).
- `FR-ANALYTICS-01` [P5] Cross-module "readiness score" view aggregates signal
  from DSA consistency, resume quality, and GitHub activity into a single
  trend chart.

### 4.7 Phase 6 — Hardening

No new functional requirements — this phase is entirely NFR-driven (see §5).

---

## 5. Non-Functional Requirements

### 5.1 Security

- `NFR-SEC-01` All endpoints validate input server-side regardless of frontend
  validation (Zod on frontend is UX, not security).
- `NFR-SEC-02` Rate limiting on all public endpoints; stricter limits on
  auth endpoints (login, password reset) to blunt brute-force/enumeration.
- `NFR-SEC-03` Passwords hashed with bcrypt/Argon2, never reversible encryption.
- `NFR-SEC-04` CSRF protection on state-changing requests; secure, httpOnly,
  SameSite cookies for refresh tokens.
- `NFR-SEC-05` Audit log for sensitive actions (login, password change, GitHub
  disconnect, account deletion) — append-only, queryable by user.

### 5.2 Performance

- `NFR-PERF-01` Lighthouse score ≥95 on marketing page, ≥90 on authenticated
  dashboard (100 is the aspiration; 90+ authenticated is the realistic gate
  given dashboard's inherent JS weight).
- `NFR-PERF-02` API p95 response time <300ms for non-AI endpoints; AI endpoints
  are exempt (network-bound to Gemini) but must show a loading state <100ms
  after request.
- `NFR-PERF-03` Long lists (DSA log, job applications, GitHub repo list) are
  paginated or virtualized past 50 items.

### 5.3 Accessibility

- `NFR-A11Y-01` WCAG 2.2 AA compliance verified via automated audit (axe) +
  manual keyboard-only pass before each phase ships.
- `NFR-A11Y-02` All interactive elements reachable and operable via keyboard;
  visible focus states on every focusable element (a common casualty of
  "premium" custom UI — flagged explicitly so it isn't skipped).

### 5.4 Reliability & Data Integrity

- `NFR-REL-01` Soft delete on user-generated content (notes, resumes, projects)
  — hard delete only on explicit account deletion.
- `NFR-REL-02` GitHub sync failures degrade gracefully — stale data shown with
  a "last synced" timestamp, never a broken page.

### 5.5 Maintainability

- `NFR-MAINT-01` No module's business logic lives in a controller/component —
  service layer enforced on backend, custom hooks enforced on frontend.
- `NFR-MAINT-02` AI Service Layer is provider-agnostic (interface-based), so
  swapping Gemini for another provider later touches one adapter, not every
  call site (per PRD §8 mitigation).

---

## 6. External Interface Requirements

| Interface | Direction | Notes |
|---|---|---|
| Google OAuth2 | Inbound (login) | Standard OAuth2 code flow |
| GitHub OAuth + REST API | Outbound (sync) | Read-only scope; backend-proxied, never client-side |
| Google Gemini API | Outbound (AI) | Backend-proxied; responses cached (`FR-AI-02`) |
| Cloudinary | Outbound (media) | Avatar uploads, certificate attachments |
| Neon PostgreSQL | Internal | Primary datastore |

---

## 7. Traceability Note

Every FR above carries a phase tag matching PRD §6. When we write
`19_Development_Roadmap.md`, each phase's sprint breakdown should trace back to
this section's FR IDs — so "what am I building this week" always maps to "why,"
per your own documentation rule.

## Next Document

`04_System_Architecture.md` — high-level system design: service boundaries,
how the frontend/backend/AI-layer/external-integrations fit together, and the
first real architecture decisions (e.g., how the AI Service Layer's
provider-agnostic interface is actually shaped). Database design
(`05_Database_Architecture.md`) follows immediately after, since per your own
rule, database comes before APIs.

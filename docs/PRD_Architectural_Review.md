# DevTrack AI — Architectural Review of PRD v2.0

**Reviewer stance:** Staff Engineer review, the kind that happens before a design doc
is allowed to move to implementation. This is deliberately critical — a review that
only confirms what's already written isn't a review.
**Reviewed document:** `02_Product_Requirements_Document.md` v2.0
**Verdict up front:** The PRD is directionally sound and unusually disciplined for a
solo project (phase gating, MoSCoW, explicit non-goals). But it has three real
architectural gaps that will bite in Phase 0 and Phase 5 specifically if not resolved
before `04_System_Architecture.md` is written. Flagging those first, then the full
category breakdown.

---

## Top 3 issues to resolve before architecture, not after

1. **No transactional email provider in the tech stack**, but `FR-AUTH-05`
   (password reset) and email verification both require one. This isn't a nitpick —
   it's a missing dependency that Phase 0 cannot actually ship without.
2. **Redis is listed as "future-ready" (i.e., not built in Phase 0), but `FR-AI-02`
   (AI response caching) is a Phase 5 requirement that needs a real cache.** Either
   Redis moves earlier, or Phase 5 needs a documented alternative (DB-backed cache
   table). Right now the PRD contradicts itself on when caching infrastructure exists.
3. **Serverless Postgres (Neon) + Spring Boot's default connection pooling (HikariCP)
   is a known failure mode**, not a hypothetical one — Neon's serverless driver
   behaves differently under connection churn than a traditional always-on Postgres
   instance, and a naive HikariCP pool config will exhaust connections under even
   moderate concurrent load. This needs to be a named decision in
   `04_System_Architecture.md`, not discovered in production.

---

## 1. Missing Assumptions

| # | Assumption not stated | Why it matters |
|---|---|---|
| 1.1 | Gemini free-tier quota numbers (requests/day, tokens/request) are never stated anywhere | You can't design caching or rate limiting (`FR-AI-02`) against a limit you haven't written down. This should be a concrete number in `09_AI_Architecture.md`, sourced from Gemini's actual published limits, not assumed generous. |
| 1.2 | No transactional email provider chosen | Flagged above — this is a Phase 0 blocker, not a nice-to-have. |
| 1.3 | GitHub connection is implied but never stated as mandatory or optional | Affects onboarding UX (§10 journey) and whether Phase 4's Project↔repo linking (`FR-PROJ-01`) has a defined behavior when no GitHub is connected. |
| 1.4 | Single resume per user, or resume versioning, is unaddressed | `FR-RESUME-01` doesn't say whether a user can have multiple resumes (e.g., one per target role, which is realistic given `FR-ATS-01` scores against a specific job description). This changes the data model materially — decide before `05_Database_Architecture.md`. |
| 1.5 | No stated assumption on how "resume claims X skill" is actually extracted for the AI correlation feature (§1 mission, `FR-AI-01`) | This is the single most-differentiating feature in the whole product, and the PRD never states whether this is structured-field comparison (easy) or free-text NLP parsing of resume prose (hard, error-prone). This is a design decision hiding as an assumption. |
| 1.6 | No solo-dev capacity buffer modeled across the 12-month, 6-phase plan | Real risk: one delayed phase (illness, job, life) has no slack built in before it cascades into every later phase's dates. Worth a stated assumption like "phases are sequenced, not calendar-locked — a delay shifts the plan, it doesn't compress it." |

## 2. Scalability Risks

| # | Risk | Notes |
|---|---|---|
| 2.1 | Neon + HikariCP connection exhaustion under concurrent load | See top-3 issues. Needs an explicit pooling strategy decision (e.g., Neon's pooled connection string / PgBouncer mode) in `04_System_Architecture.md`. |
| 2.2 | `FR-GH-02` ("sync on a schedule") has no scheduler component in the stack | Spring's `@Scheduled` works fine on a single instance but double-fires if Railway ever runs >1 instance. Either commit to single-instance for v1 (fine, but say so) or introduce a proper job mechanism. This is a decision, not an accident to discover later. |
| 2.3 | Portfolio CMS public pages (`FR-PORT-02`) are unauthenticated and could receive unpredictable traffic (a portfolio page can go viral on its own, independent of the app's actual user growth) | These pages are currently architected to be served by the same backend/DB as authenticated API traffic. A traffic spike on one user's public portfolio page can degrade the app for everyone. Worth considering static generation / ISR / CDN caching for this one surface specifically — it's the one part of the product that isn't behind auth and therefore has fundamentally different scaling characteristics. |
| 2.4 | No stated fair-use limit per user on AI queries | One heavy user can exhaust the entire app's Gemini quota (1.1) for every other user. This is both a scalability and a security/abuse concern — see §3.6. |
| 2.5 | Notification delivery mechanism (polling vs. push) undecided | Not urgent at Phase 0 scale, but worth a one-line stated decision now (e.g., "polling is acceptable for v1 given expected concurrent user counts") so it's a decision, not a gap discovered in Phase 5. |

## 3. Security Concerns

| # | Concern | Notes |
|---|---|---|
| 3.1 | Portfolio CMS composes data from private user records into a public page | The real risk isn't the feature, it's implementation drift — if the public portfolio serializer ever reuses the same DTO as an authenticated internal endpoint, a field added later (email, internal notes) leaks publicly by accident. This needs an explicit rule in `12_Security.md`: public portfolio responses use an allowlist DTO, never a shared/internal one. |
| 3.2 | Certificate "verification URL" (`FR-CERT-01`) — if the backend ever fetches that URL server-side to validate it, that's a textbook SSRF vector | Worth deciding now whether verification is even server-fetched in v1, or just stored as a link for humans to click. Simpler is safer here. |
| 3.3 | Resume/certificate file uploads have no stated file-type/malware validation requirement | Needs to be in `12_Security.md` explicitly — "trust the file extension" is not a security control. |
| 3.4 | GitHub OAuth tokens — encryption-at-rest not stated | Third-party OAuth tokens are a high-value target (a leaked token gives an attacker read access to a user's GitHub). Should be explicit, not assumed. |
| 3.5 | CORS policy unspecified for a cross-origin setup (Vercel frontend, Railway backend) | Needs a strict origin allowlist, stated explicitly rather than left to default framework behavior (which is often permissive in dev and easy to forget to lock down for prod). |
| 3.6 | No abuse/cost-control layer on AI endpoints beyond generic rate limiting | `NFR-SEC-02` covers general rate limiting, but AI endpoints need a *cost-aware* limit specifically (e.g., N AI queries/user/day), because the failure mode isn't just "slow," it's "the whole app's AI quota is gone." This deserves its own NFR, not to be lumped into generic rate limiting. |
| 3.7 | No session/device management mentioned (view active sessions, revoke a specific device) | Not a blocker for v1, but worth a stated non-goal rather than silence, same pattern as other gaps here — an omission should be a decision, not an accident. |

## 4. AI Integration Risks

| # | Risk | Notes |
|---|---|---|
| 4.1 | Context window budget for `FR-AI-01` (cross-module correlation) is undesigned | Assembling resume + DSA history + GitHub stats + job data into one prompt has a real token cost that grows with user activity. A power user's context could get expensive or hit model limits. This needs a concrete strategy (summarization, selective inclusion, retrieval) in `09_AI_Architecture.md`, not "send everything." |
| 4.2 | No degraded-mode UX designed for when Gemini is rate-limited or down | The PRD states the *principle* (§10: "the product must not depend on the AI layer to be useful") but never designs what the AI Assistant screen actually shows in that state. That's a real UI state that needs a mock/spec before Phase 5, not an afterthought. |
| 4.3 | PII exposure to a third-party AI provider is unaddressed | Resume content is sent to Gemini for analysis (`FR-ATS-01`) — this has real privacy-policy and ToS implications (what Google's API terms say about data retention/training use) that should be checked and stated, not assumed fine. |
| 4.4 | Prompt injection risk is *named* in the PRD (§14) but has zero concrete mitigation — "detailed handling in 09/12" is a placeholder, not a plan | Flagging so it doesn't quietly stay a placeholder forever — this needs to be one of the first things designed in `09_AI_Architecture.md`, since uploaded resume/notes content is exactly the kind of untrusted input that becomes a real prompt-injection vector once it's concatenated into an LLM call. |
| 4.5 | Six distinct "AI agents" (§12 of PRD) is a lot of independent prompt-strategy surface for a solo developer to design, tune, and maintain well | Recommend, as a design opportunity (see §6 below), consolidating toward fewer, more general reasoning services with different context injection, rather than six fully bespoke agents each needing their own prompt strategy, fallback, and caching design. |

## 5. Feature Dependency Issues

| # | Issue | Notes |
|---|---|---|
| 5.1 | `FR-CAL-02` (Calendar surfaces deadlines from other modules) ships in Phase 1, but its actual data sources (Job Tracker, Phase 3) don't exist yet | The SRS already flags this as a forward dependency needing an event-subscription pattern — but that pattern isn't designed anywhere yet. Real risk: it gets implemented ad hoc per-module later, creating tight coupling between Calendar and every module that "surfaces" into it. This deserves a real design (e.g., a lightweight internal event/subscription mechanism) in `04_System_Architecture.md`, before Phase 1 code is written — not retrofitted in Phase 3. |
| 5.2 | Notifications (Phase 0) and RBAC-for-multiple-roles (Phase 0) are both built before any consumer/use case exists | This is worth naming directly: building infrastructure for a future need that isn't concrete yet is a real YAGNI risk for a solo, time-boxed project. See §6 for the simplification recommendation. |
| 5.3 | AI Portfolio Reviewer agent and its Phase 4 data dependency (Portfolio CMS) both land in different phases with zero buffer — Portfolio Reviewer ships Phase 5, immediately after its dependency ships in Phase 4 | Low risk on its own, but combined with 5.1's coupling risk and the AI layer's general complexity (4.5), Phase 5 is carrying more sequencing risk than any other phase. Worth explicitly treating Phase 5 as the highest-risk phase in the roadmap doc, with slack. |
| 5.4 | GitHub connection optionality (1.3) cascades into a real dependency question: what does Portfolio CMS (`FR-PORT-01`) render for a user who never connected GitHub? | Needs a stated fallback (e.g., portfolio still works from manually-entered Projects/Certificates alone) or GitHub connection needs to be a hard prerequisite — currently undefined either way. |

## 6. Opportunities to Simplify or Strengthen

1. **Defer full RBAC design; hardcode a single-role check for v1.** The PRD's own
   justification ("extensible for future roles") is speculative for a single-user
   product. Keep the *interface* extensible (don't hardcode role checks inline), but
   don't build a role-management system with nothing to manage yet. Saves real Phase 0
   time.
2. **Defer Notifications infrastructure to Phase 2**, when the first real producer
   (GitHub sync completion, DSA streak milestone) actually exists, instead of building
   and testing an empty consumer against seed data in Phase 0. Cleaner to build a
   feature against a real producer than to guess at the interface twice.
3. **Introduce a minimal internal event-publishing pattern in Phase 0** (even a simple
   in-process event bus, not a message queue) specifically to solve 5.1 cleanly —
   Calendar and Notifications both need "something happened in another module," and
   solving it once, early, is cheaper than solving it twice, later, under deadline
   pressure.
4. **Write down a concrete AI budget policy** (e.g., "N free AI queries/user/day, hard
   cap, clear UI messaging when hit") — this turns 1.1/2.4/3.6 from three separate
   loose risks into one designed constraint with a clear owner (`09_AI_Architecture.md`).
5. **Consider static generation or CDN caching specifically for Portfolio CMS pages**
   (`FR-PORT-02`), decoupling the one public, auth-free surface from the authenticated
   API's capacity — addresses 2.3 directly and is a genuinely simpler architecture, not
   just a safer one.
6. **Consolidate the six AI agents (§12 of PRD) conceptually**, at least for v1, into
   fewer backend services differentiated by prompt/context rather than six fully
   independent agent identities. The user-facing product can still *feel* like six
   distinct capabilities; the backend doesn't need six times the prompt-engineering
   and fallback-design surface area for a solo build.
7. **Add the missing email provider to the tech stack now** (e.g., Resend or SendGrid)
   — small addition, but it's a genuine Phase 0 blocker currently invisible in the
   stack list.

---

## Summary Table

| Category | Critical (blocks Phase 0/architecture) | Should resolve before relevant phase | Worth a stated decision, low urgency |
|---|---|---|---|
| Missing assumptions | 1.1, 1.2 | 1.4, 1.5 | 1.3, 1.6 |
| Scalability | 2.1 | 2.2, 2.3 | 2.4, 2.5 |
| Security | — | 3.1, 3.4, 3.6 | 3.2, 3.3, 3.5, 3.7 |
| AI integration | 4.4 | 4.1, 4.2, 4.3 | 4.5 |
| Feature dependencies | 5.1 | 5.3, 5.4 | 5.2 |

**Recommendation:** resolve everything in the "Critical" column before writing
`04_System_Architecture.md` — those five items (email provider, Redis/caching timing,
Neon connection pooling, prompt-injection handling, the calendar event-dependency
pattern) all directly shape architecture decisions rather than sitting cleanly inside
implementation. Everything else can be resolved as its phase approaches without
re-architecture risk.

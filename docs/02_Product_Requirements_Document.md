# DevTrack AI — Product Requirements Document (PRD)

**Document owner:** Product/Engineering (you, solo)
**Status:** Draft v2.0
**Timeline commitment:** 12 months, solo developer, part-time steady pace
**Last updated:** 2026-08-06

---

## 1. Vision, Mission, Values & Product Principles

**Mission**
Give every developer a single system of record for their own growth — code,
skill, and career — so progress is visible instead of scattered.

**Vision**
Become the operating system developers open every day, the way GitHub is the
system of record for code and LinkedIn is the system of record for a career
history — except DevTrack AI is the system of record for *becoming* employable,
not just proving you already are.

**Values**
- **Evidence over claims.** A resume line means nothing until it's backed by
  tracked activity. The product's entire differentiation depends on enforcing this.
- **One system, not one more tab.** Every module must justify its existence by
  connecting to at least one other module. A module that's an island gets cut.
- **Depth over breadth in AI.** One AI feature that correctly reasons across
  real user data beats five AI features that each independently call an LLM
  with no context.

**Product Principles**
1. If a feature can't answer "what data does this consume from another module,"
   it doesn't belong in v1.
2. Empty states are a designed product surface, not an afterthought — most
   users will see them before they see populated data.
3. Never ship an AI feature that can't gracefully degrade when the AI provider
   is rate-limited or down (see §14).

---

## 2. Executive Summary

DevTrack AI is a single, AI-native platform that unifies the tools a developer
currently scatters across GitHub, LeetCode, Notion, Linear, a resume builder,
and a job tracker. Instead of stitching together five subscriptions and three
spreadsheets, a developer logs into one dashboard that understands their code
activity, their DSA progress, their resume quality, and their job search — and
connects the dots between them.

The wedge is not "another dashboard." The wedge is **AI-derived correlation
across a developer's own data**: DevTrack AI can tell a user their resume claims
"strong DSA skills" while their tracked practice shows 12 solved problems in
3 months, or that their GitHub activity dropped the same week their study streak
broke. No single existing tool does this because no single existing tool has
both data sources.

This PRD scopes a 12-month, solo-built, production-grade SaaS covering all 20
requested modules, sequenced into 6 delivery phases so the product is demoable
and useful after every phase — not just at month 12.

## 3. Problem Statement

Developers preparing for interviews, building portfolios, or growing their
careers currently manage:

- Code/projects → GitHub
- DSA practice → LeetCode / a spreadsheet
- Notes and study plans → Notion / Obsidian
- Task/project tracking → Linear/Trello
- Resume → Word/Canva, with no idea if it's ATS-safe
- Job applications → a spreadsheet, if tracked at all
- Interview prep → scattered notes, no feedback loop

None of these tools talk to each other. A user cannot answer "am I actually
ready for this job?" from a single source of truth, because the evidence
(code activity, DSA consistency, resume content, application outcomes) lives
in five disconnected places.

## 4. Competitive Landscape

Every tool below does one slice of this well. None does the correlation.

| Competitor | What they do | What DevTrack does better |
|---|---|---|
| **GitHub** | Code hosting, contribution graph, collaboration | DevTrack reads GitHub as *one input signal* and correlates it against resume claims, DSA consistency, and job requirements — GitHub itself has no concept of "career readiness." |
| **LeetCode** | DSA practice, problem sets, contests | DevTrack tracks the same practice data but ties it to *why it matters* — resume gap analysis, readiness scoring, study planning — rather than practice for its own sake. |
| **Notion** | General-purpose notes/docs, fully manual | DevTrack's Notes/Study Planner are purpose-built for developer career workflows with structured, queryable data (streaks, tags) — not a blank canvas requiring the user to build their own system. |
| **Linear** | Team-grade issue tracking, built for engineering orgs | DevTrack's Project Management is deliberately lighter — solo/portfolio project tracking linked to real GitHub repos, not a team workflow tool. Not a replacement for Linear at a company; not trying to be. |
| **Obsidian** | Local-first personal knowledge graph | DevTrack trades Obsidian's infinite flexibility for structure — notes are linked to career data by design, not by the user manually building backlinks. |
| **Atlassian (Jira/Confluence)** | Enterprise project + knowledge management | Out of DevTrack's lane entirely — Atlassian is org-scale; DevTrack is individual-scale. Listed for completeness, not real overlap. |
| **Cursor** | AI-native code editor | Different layer of the stack — Cursor helps you *write* code; DevTrack helps you *prove and grow* from the code you've already written. Complementary, not competing. |
| **Google Calendar** | General scheduling | DevTrack's Calendar is narrower on purpose — it surfaces deadlines *generated by other modules* (job application deadlines, study milestones), not general-purpose event scheduling. |
| **ChatGPT** | General-purpose AI chat | DevTrack's AI Assistant has zero value without DevTrack's own data — it's not a chat wrapper, it's a reasoning layer over a specific user's tracked activity. A generic ChatGPT session doesn't know the user's DSA streak or resume content. |

**Honest framing, not spin:** DevTrack does not out-compete any of these tools
on their core job. It doesn't out-code-host GitHub or out-practice LeetCode.
The bet is that *connecting* these signals is more valuable than any one of
them individually — and that's an unproven bet, not a guaranteed one. Worth
stating plainly rather than pretending this is a solved competitive position.

## 5. Market Sizing (illustrative, not verified market research)

These are directional numbers for narrative/portfolio purposes — not a funding
memo. A real go-to-market would require actual primary research; that's out of
scope for a solo portfolio SaaS and would be a wasted effort at this stage.

| Tier | Definition | Rough size |
|---|---|---|
| **TAM** | All software developers + CS/self-taught learners globally | ~40–50M (developer population estimates cluster around 25–30M professional devs; adding students and self-taught learners roughly doubles that) |
| **SAM** | English-speaking developers actively job-hunting or upskilling (the group this product's workflow actually fits) | ~8–12M |
| **SOM** | Realistic reachable users for a solo-built, unfunded v1 with no marketing budget, in year one | Low thousands (hundreds to low-thousands of signups is a realistic year-one ceiling without paid acquisition — worth being honest that SOM at this stage is a credibility exercise, not a growth plan) |

If this ever becomes more than a portfolio project, market sizing should be
redone with actual sourced data before it appears in anything shown to a real
investor or stakeholder.

## 6. Goals

1. Give developers **one login** that replaces the five-tool sprawl above for
   the specific job of *career readiness and execution*.
2. Make AI **structurally load-bearing**, not decorative — every AI feature
   must consume the user's own cross-module data, not operate as a generic
   chatbot bolted on.
3. Ship something that is genuinely deployable and demoable after **every
   phase**, not just at the end of 12 months.
4. Produce a codebase and architecture that would survive a real code review
   at a product company — this is the actual portfolio artifact, not just the
   live app.

### Non-Goals (explicitly out of scope for v1)

- Team/organization accounts, multi-tenant billing, or admin consoles.
- Real-time collaborative editing (e.g., multiplayer Notion-style docs).
- Native mobile apps (responsive web only).
- Marketplace/community features (sharing templates publicly, public
  leaderboards) — flagged as a strong v2 candidate, not v1.
- Payment processing / paid tiers. v1 is free; monetization design is deferred
  until there's a retained user base to monetize (called out as a risk in §14).
- **Recruiter-facing features** (candidate search, employer dashboards). Flagged
  explicitly: no module in this PRD serves recruiters, so "as a recruiter..."
  user stories are not included in §8 — this is a call for you to make, not a
  default I'm silently applying. If recruiter-facing discovery is actually
  wanted, it's a scope addition, not a missing user story.

## 7. Target Users

**Primary personas**

| Persona | Core need | Primary modules used |
|---|---|---|
| CS/BCA/MCA student prepping for placements | Structure DSA + resume + applications | DSA Tracker, Study Planner, Resume Builder, Job Tracker |
| Self-taught developer building a portfolio | Prove skill without a CS degree | GitHub Analytics, Portfolio CMS, Project Management |

**Secondary personas**

| Persona | Core need | Primary modules used |
|---|---|---|
| Working engineer job-switching | Track applications, tailor resume per role | Resume Analyzer/ATS, Job Tracker, Interview Tracker |
| Open-source contributor | Show real contribution signal | GitHub Analytics, Portfolio CMS |

## 8. User Stories (MoSCoW-tagged)

Grouped by persona. Priority tags: **M**ust / **S**hould / **C**ould / **W**on't
(v1). These drive the FR list in the SRS — every Must-have story should trace
to at least one `FR-*` in `03_Software_Requirements_Specification.md`.

**As a student preparing for placements:**
- **[M]** I want to log DSA problems I've solved so I can see my consistency
  over time, not just a raw count.
- **[M]** I want to build a resume and know if it's ATS-safe before I submit it.
- **[M]** I want to track which companies I've applied to and at what stage,
  so I stop losing track in a spreadsheet.
- **[S]** I want a study plan with streaks so I stay accountable day to day.
- **[C]** I want AI to tell me which DSA topics I'm weak in based on my actual
  solve history, not a generic study guide.
- **[W] (v1)** I want to compare my progress against other students anonymously
  — explicitly deferred; this is a leaderboard/community feature (§6 non-goal).

**As a self-taught developer building a portfolio:**
- **[M]** I want my GitHub activity analyzed and shown in a way that proves
  real contribution, not just repo count.
- **[M]** I want a public portfolio page generated from my real tracked
  projects, not something I have to manually re-type.
- **[S]** I want to link project management tasks to the actual repo I'm
  building, so my planning and my code stay connected.
- **[C]** I want AI to review my portfolio and flag what's missing compared
  to what employers in my target role typically look for.

**As a working engineer switching jobs:**
- **[M]** I want to tailor my resume per job description and see a keyword-gap
  score before I apply.
- **[M]** I want to log interview rounds and outcomes so I can see patterns
  in where I'm losing candidates (technical vs. behavioral, etc.).
- **[S]** I want reminders/calendar entries auto-created from my job tracker
  deadlines so I don't rely on manually remembering them.

**As an open-source contributor:**
- **[S]** I want my contribution history summarized in a way I can put in
  front of a hiring manager, without manually compiling stats.
- **[C]** I want certificates and contributions to show up together on my
  public portfolio as combined proof of skill.

## 9. Success Metrics & KPIs

"Good product" isn't measurable. These are.

**Activation & engagement**
- Signup → GitHub connected → first dashboard data populated, in <5 minutes.
- Monthly Active Users (MAU), Weekly Active Users (WAU), WAU/MAU ratio (stickiness).
- Average session length and sessions/week per retained user.
- Weekly-active-usage of ≥2 modules per retained user (proves it's a system,
  not a single-feature tool — directly ties back to §1's product principle).

**Retention**
- Day-7 and Day-30 retention (signup cohort).
- Module-specific retention: % of users still logging DSA problems / updating
  job tracker 30 days after first use.

**Feature-level counters** (also feeds §11 analytics events)
- Resumes created, resume analyses run, AI prompts sent, DSA problems logged,
  GitHub accounts connected, jobs tracked, interviews logged, certificates added,
  study plans created, portfolio pages published.

**Technical health**
- API p95 response time (target defined in SRS `NFR-PERF-02`).
- AI feature usage rate and Gemini quota consumption trend (directly informs
  the caching/rate-limit risk in §14).

**Engineering-credibility track** (the actual primary success metric for a
solo portfolio-flagship project, stated honestly)
- Clean architecture a senior engineer would approve in code review.
- 100 Lighthouse performance score on the marketing page and dashboard shell.
- Meaningful test coverage on critical flows (auth, resume analysis, GitHub sync).
- A README and architecture docs that let a stranger understand and run the
  project in under 15 minutes.

## 10. User Journey

The primary end-to-end funnel this product is designed around:

```
Landing Page
     ↓
Signup (email or Google OAuth)
     ↓
Dashboard (empty state, guided onboarding checklist)
     ↓
Connect GitHub  →  GitHub Analytics populates
     ↓
Import / Build Resume  →  ATS Analyzer scores it
     ↓
AI generates a starting roadmap (weak DSA topics + resume gaps vs. target role)
     ↓
Track DSA practice against that roadmap  →  streaks build
     ↓
AI Assistant surfaces cross-module suggestions
     (e.g., "resume claims backend strength; DSA log shows mostly frontend-tagged problems")
     ↓
Apply to a job  →  Job Tracker entry created
     ↓
Log interview round(s)  →  Interview Tracker
     ↓
Offer (or rejection) logged  →  feeds back into AI Assistant's future suggestions
```

Note: the "AI generates a starting roadmap" step only becomes real in Phase 5
(§12). Before that, this journey still functions manually — a user can still
connect GitHub, build a resume, track DSA, and apply to jobs with zero AI
involvement. This matters: **the product must not depend on the AI layer to be
useful**, both because Phase 5 ships last and because Gemini quota/downtime
(§14) must never fully block the core workflow.

## 11. Module Inventory & Phasing

20 modules is not buildable as a flat list — it's buildable as a sequence where
each phase ships something a user could actually log in and use. Each phase
assumes ~2 months at your steady part-time pace (12 months / 6 phases).
MoSCoW column reflects v1 priority per module.

### Phase 0 — Foundation (Month 1) — **Must**
- Authentication (email/password + Google OAuth2, JWT + refresh token rotation)
- Authorization (role-based, extensible for future roles even though v1 is single-user)
- Dashboard shell (layout, navigation, command palette scaffold, theme system)
- Database schema foundation + migrations pipeline
- CI/CD skeleton (GitHub Actions → Vercel + Railway), Docker Compose for local dev
- Notifications (infrastructure only)
- Profile, Settings

**Exit criteria:** a user can sign up, log in, see an empty but fully-styled
dashboard, and the deploy pipeline pushes to a live URL on merge to main.

### Phase 1 — Core Productivity (Month 2–3) — **Should**
- Notes, Calendar, Study Planner

**Exit criteria:** a user has a reason to open the app daily even before any
AI or career features exist.

### Phase 2 — Developer Signal (Month 4–5) — **Must**
- GitHub Analytics, DSA Tracker, Certificates

**Exit criteria:** the app now has real data about the user's actual developer
activity — this is the data the AI layer and career modules will later
correlate against.

### Phase 3 — Career Toolkit (Month 6–7) — **Must**
- Resume Builder, Resume Analyzer/ATS Checker, Job Tracker, Interview Tracker

**Exit criteria:** a user can build a resume, get it scored, apply to tracked
jobs, and log interview outcomes — the "job search operating system" is
functional standalone.

### Phase 4 — Build & Show (Month 8–9) — **Should**
- Project Management, Portfolio CMS

**Exit criteria:** a user can generate a public portfolio page that's backed
by real tracked data, not manually re-typed content.

### Phase 5 — AI Integration Layer (Month 10) — **Must** (this is the differentiator)
- AI Assistant (cross-module correlation, not general chat), Analytics
  (readiness score, trend charts)

**Exit criteria:** the AI Assistant can answer questions that require joining
data across at least 3 modules.

### Phase 6 — Hardening & Polish (Month 11–12) — **Must**
- Accessibility audit, performance pass, security review, visual polish pass
  (3D background, aurora effects, micro-interactions — deliberately last),
  test coverage backfill, documentation pass.

> **Explicit tradeoff, stated plainly:** visual premium-ness is scheduled last
> so it doesn't consume time the working product needs first. Reviewable, not
> sacred — say so if you want it pulled earlier for demo purposes.

## 12. AI Agent Architecture — Preview

Full detail (inputs, outputs, prompt strategy, fallback, caching, confidence,
context window, memory) belongs in **`09_AI_Architecture.md`**, not here —
duplicating it in both places guarantees the two documents drift out of sync
the first time either one is updated. This section exists only so the PRD
names *what* AI capability ships and *why*, at product-decision depth.

| Agent | Purpose | Ships in |
|---|---|---|
| AI Career Agent | Cross-module readiness reasoning (the core differentiator, `FR-AI-01`) | Phase 5 |
| AI Resume Agent | Powers ATS scoring and gap analysis against a target job description | Phase 3 |
| AI GitHub Agent | Surfaces contribution insight beyond raw stats (e.g., consistency, language depth) | Phase 5 (built on Phase 2 data) |
| AI Study Planner Agent | Suggests study focus based on tracked DSA weak spots | Phase 5 (built on Phase 2 data) |
| AI Portfolio Reviewer | Flags portfolio gaps vs. target role | Phase 5 (built on Phase 4 data) |
| AI Interview Coach | Pattern analysis on logged interview outcomes | Backlog — **Could-have**, not committed to v1; depends on enough real interview data existing per user, which is unlikely in year one |

## 13. Analytics Events (v1 instrumentation list)

Concrete events to track from day one so KPIs in §9 are actually measurable,
not retrofitted later:

`user_signed_up`, `user_logged_in`, `github_connected`, `github_synced`,
`resume_created`, `resume_exported`, `resume_analyzed`, `dsa_problem_logged`,
`study_plan_created`, `certificate_added`, `job_application_created`,
`job_application_stage_changed`, `interview_logged`, `project_created`,
`portfolio_published`, `ai_prompt_sent`, `ai_response_cached_hit`,
`notification_viewed`.

Each should carry a `userId`, `timestamp`, and event-specific payload. Exact
schema belongs in implementation, not this PRD — noted here so it's designed
in from Phase 0, not bolted on in Phase 6.

## 14. Risks & Open Questions

| Risk | Why it matters | Mitigation |
|---|---|---|
| Solo scope creep across 20 modules | #1 killer of ambitious solo projects | Hard phase gates (§11); each phase must reach exit criteria before the next starts |
| Gemini free-tier rate limits / quota exhaustion | AI Assistant is central, not decorative | Provider-agnostic AI Service Layer, aggressive response caching, graceful degradation (§10 note) |
| AI hallucination in career/resume advice | Wrong advice actively hurts a user's job search — this is a real-consequence AI feature, not a toy | Ground every AI response in the user's actual tracked data rather than open-ended generation; surface confidence/sourcing in the UI (full design in `09_AI_Architecture.md`) |
| Prompt injection (e.g., via uploaded resume text or notes) | Uploaded user content is untrusted input the moment it reaches an LLM call | Treat all user-supplied content as data, not instructions, when constructing prompts — detailed handling in `09_AI_Architecture.md` and `12_Security.md` |
| AI latency | Career/resume analysis is not instant; users will notice | Loading states <100ms after request (`NFR-PERF-02`), async processing where feasible |
| GitHub OAuth API rate limits at scale | Analytics module depends on it | Cache repo snapshots, don't poll live on every dashboard load |
| No monetization designed | v1 is free — fine for portfolio, but worth deciding early if this ever needs to sustain itself | Deferred by design (§6 non-goal); revisit post-v1 if real users show up |
| 3D/animation scope inflation | Easy to lose weeks polishing visuals | Scheduled deliberately in Phase 6, time-boxed |

**Open questions for you to decide before Phase 0 architecture doc:**
1. Should the Portfolio CMS output be a subdomain or a path? *(Resolved with a
   default in the SRS §2 — override there if you disagree.)*
2. GitHub OAuth — read-only only, or write access later? *(Also resolved with
   a default in SRS §2.)*
3. Resume export — PDF only, or also DOCX? *(Also resolved in SRS §2.)*
4. **New from this revision:** should the AI Interview Coach agent (§12) be
   promoted from Could-have to committed scope, or left as backlog? Leaving
   it as backlog for now since it depends on interview-outcome data volume
   that's unlikely to exist per-user in year one — flag if you disagree.

## 15. Non-Functional Requirements (summary — full detail in later docs)

- **Security:** JWT + rotating refresh tokens, RBAC, rate limiting, input
  validation on every boundary, CSRF/XSS protection, secure cookies, audit
  logging. Full threat model, JWT/refresh/OAuth flow diagrams, and RBAC design
  belong in `12_Security.md` — not duplicated here.
- **Performance:** 100 Lighthouse target, code-splitting, lazy loading, image
  optimization, virtualization on long lists.
- **Accessibility:** WCAG 2.2 AA, full keyboard navigation, semantic HTML,
  managed focus states.
- **Testing:** JUnit/Mockito on backend, Vitest/RTL on frontend, critical-path
  coverage mandatory.
- **Database strategy** (why PostgreSQL over alternatives, indexing,
  partitioning, caching, search, storage) belongs in `05_Database_Architecture.md`.
- **UI/design philosophy** (typography, spacing, motion, color tokens, design
  language) belongs in `10_UI_UX_Design_System.md`.

Each of these four gets full dedicated treatment in its own document — that's
why the file structure you defined already has them as separate files. Putting
them here too would just create two sources of truth for the same decisions.

---

## Next Document

`03_Software_Requirements_Specification.md` — translates this PRD into concrete
functional/non-functional requirements with acceptance criteria, ready to drive
the Phase 0 architecture design.

Per your own working rule ("never skip planning," "database comes before
APIs"), the next thing we should actually design after the SRS is the **Phase 0
data model**, since Auth/Authz/Profile/Settings all depend on it.

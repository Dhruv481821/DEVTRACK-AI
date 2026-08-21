# DevTrack AI — Development Roadmap

**Status:** Draft v1.0
**Depends on:** the entire document set
**This is where phase/FR-ID progress is tracked** (`18_Claude_Workflow.md` §5) — the
tables below get updated as work actually happens, not written once and left static.

**Honest starting state:** zero code has been written yet. Everything below is the
plan, not a status report of work completed — every item starts at **Not Started**.

---

## 1. Scope of This Document (per `18_Claude_Workflow.md` §2)

**Only Phase 0 gets a sprint-level breakdown here.** Phases 1–5 stay at the PRD's
phase-level granularity (§11 of the PRD) until each one gets its own mini-planning
pass — extending `05_Database_Architecture.md` and `06_API_Specification.md` with
real schema/endpoint detail — immediately before that phase actually starts. Writing
a detailed 12-month, 6-phase sprint plan today would mean designing Phase 5's AI
endpoints before Phase 1's Notes schema even exists in code — exactly the premature
detail this doc set has avoided everywhere else. §3 below lists what triggers each
future phase's planning pass.

## 2. Phase 0 — Sprint Breakdown (Month 1, ~4 weeks)

| Week | Focus | `FR-*` IDs | Key deliverable | Primary docs |
|---|---|---|---|---|
| 1 | Foundation scaffolding | — | Repo structure (`07`/`08` §1 package layout), Docker Compose (`14` §2), Neon provisioned, Flyway baseline migration applied, CI lint+test pipeline running (deploy steps come week 4) | `05` §7, `07`, `08`, `14` §2–3 |
| 2 | Auth backend | `FR-AUTH-01..05`, `FR-AUTHZ-01` | Registration, login (password + Google OAuth), refresh rotation + reuse detection, password reset, RBAC scaffold, Bucket4j in-memory rate limiting — all with the critical-path tests from `13_Testing.md` §4 passing | `05` §7, `06` §2.1, `07`, `12` §2–3 |
| 3 | Profile/Settings/Notifications backend + Auth frontend | `FR-PROF-01`, `FR-SET-01`, `FR-NOTIF-01`, `FR-DASH-01/02` | Backend endpoints for profile/settings/notifications; frontend login/register forms + the 401→refresh→retry flow (`08` §4) wired to a real backend; dashboard shell with empty states | `06` §2.2–2.3, `08`, `10`, `11` |
| 4 | Deploy + close the loop | — | Full `15_Deployment.md` §1 walkthrough executed for real, smoke test (`15` §4) passing against a live URL, `phase-0-complete` tag cut (`16_Git_Workflow.md` §5) | `15`, `16` §5 |

## Definition of Done for Phase 0

- [x] A user can register, verify email, and log in (password or Google) — verified in-browser 2026-08-10; email verification link-click flow itself not yet manually walked through (registration/login/Google all confirmed)

- [x] Refresh token rotation + reuse detection works and is tested

- [x] Dashboard shell renders with designed empty states, not blank pages — verified in-browser 2026-08-13

- [x] Profile and settings are editable — verified in-browser 2026-08-13

- [x] Live URL passes the `15_Deployment.md` §4 smoke test — verified in-browser 2026-08-17 against the deployed Neon + Railway + Vercel stack

- [x] CI pipeline runs backend/frontend lint + tests + frontend build and deploys to Railway on successful push to `main` — verified in GitHub Actions 2026-08-20; `backend`, `frontend`, and `deploy-backend` all passed

- [ ] `phase-0-complete` tag exists

## 3. Future Phase Triggers (not detailed yet — by design)

| Phase | Starts its mini-planning pass when... |
|---|---|
| 1 (Notes, Calendar, Study Planner) | Phase 0's Definition of Done is fully checked |
| 2 (GitHub Analytics, DSA Tracker, Certificates) | Phase 1 ships — also when Redis actually gets stood up (`04_System_Architecture.md` §3.2) |
| 3 (Resume, ATS, Job Tracker, Interview Tracker) | Phase 2 ships |
| 4 (Project Management, Portfolio CMS) | Phase 3 ships |
| 5 (AI Assistant, Analytics) | Phase 4 ships — also when `GEMINI_API_KEY` actually gets provisioned (`15_Deployment.md` §2) |
| 6 (Hardening & polish) | Phase 5 ships |

## 4. Progress Table (update as you go)

| Phase | Status | Tag |
|---|---|---|
| 0 — Foundation | Complete — auth, profile/settings/notifications backend, dashboard shell, frontend, live deployment, and gated CI/CD verified | phase-0-complete |
| 1 — Core Productivity | Not Started | — |
| 2 — Developer Signal | Not Started | — |
| 3 — Career Toolkit | Not Started | — |
| 4 — Build & Show | Not Started | — |
| 5 — AI Integration | Not Started | — |
| 6 — Hardening & Polish | Not Started | — |

---

## Next Document

`20_Professional_README.md` — the last document in the set: the public-facing README
that presents this project (and, honestly, this entire planning process) to whoever
finds the repo — recruiters included, per the PRD's own stated goal.

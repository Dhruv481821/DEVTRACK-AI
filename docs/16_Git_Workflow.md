# DevTrack AI — Git Workflow

**Status:** Draft v1.0
**Depends on:** `15_Deployment.md`
**Feeds into:** `17_Coding_Standards.md`, `19_Development_Roadmap.md`

---

## 1. Branch Strategy

**Decision: trunk-based, `main` is the only long-lived branch — no `develop`
branch.** A `develop`/`main` split exists to buffer integration risk across multiple
contributors merging in parallel; for a solo developer where every merge to `main`
already triggers the full CI gate (`14_DevOps.md` §3) before deploying, a second
long-lived branch would be pure ceremony with no one to buffer against. This is the
same "don't build infrastructure for a coordination problem you don't have" judgment
applied in `15_Deployment.md` §1 to the staging-environment decision.

**Feature branches, short-lived, named by module + intent:**
`feat/auth-refresh-rotation`, `fix/resume-ats-scoring-edge-case`,
`refactor/github-sync-caching`. The module name in the branch matches the backend/
frontend module names established in `07_Backend_Architecture.md` §1 /
`08_Frontend_Architecture.md` §1 — a branch name should tell you which module folder
the change lives in before you even open it.

## 2. Commit Convention — Conventional Commits, Scoped by Module

Per your own working rule. Format: `type(scope): description`, where `scope` is the
module name (matching the folder structure, same reasoning as branch naming):

```
feat(auth): add refresh token rotation with reuse detection
fix(resume): correct ATS score rounding on partial keyword matches
docs(ai-architecture): add Gemini quota research and fair-use policy
refactor(github): extract sync logic into dedicated service
test(ownership): add shared assertOwnership() coverage across modules
perf(dashboard): virtualize job application list
ci(pipeline): add Dependabot for Maven ecosystem
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `style`, `perf`, `ci`, `chore` — per
your original list. **No huge commits** (also your original rule) — a commit should
represent one coherent change, small enough that its own message can describe it in
one line without an "and" doing a lot of work.

## 3. Pull Requests — Solo-Dev Self-Review Discipline

There's no second engineer to review this code. That's a real gap, not something to
pretend away — the PR process below exists specifically to substitute a structured
self-review for the peer review a team would normally provide, not to perform ceremony
for its own sake.

**Every PR maps to something traceable** — ideally one or a small cluster of related
`FR-*` IDs from `03_Software_Requirements_Specification.md`, so "what shipped in this
PR" always answers "why" by reference, per the traceability principle the SRS itself
set up in its §7.

**PR template (self-review checklist, checked before merging, not just before
requesting a review that doesn't exist):**

```markdown
## What / Why
Closes: FR-XXX-NN

## Checklist
- [ ] Tests added per `13_Testing.md` — critical-path items (§4) covered if touched
- [ ] New env vars? → updated `15_Deployment.md` §2 checklist
- [ ] New module/package? → updated `07_Backend_Architecture.md` §1 or
      `08_Frontend_Architecture.md` §1 structure diagrams
- [ ] Touches auth/ownership/security-sensitive code? → re-read `12_Security.md`'s
      relevant section before merging, not after
- [ ] Migration included? → additive/backward-compatible per `14_DevOps.md` §4,
      or the breaking-change window is explicitly acceptable
- [ ] Conventional Commit messages throughout
```

This checklist is intentionally short — a 20-item checklist gets rubber-stamped; a
6-item one that maps directly to real decisions already made in this doc set is more
likely to actually change behavior before a merge.

## 4. Merge Strategy

**Squash merge onto `main`.** Feature-branch history can be messy — WIP commits,
"fix typo," "actually fix it" — squashing means `main`'s history is one clean,
Conventional-Commit-formatted entry per PR, which is what actually matters for a
repo meant to be read later (by you in month 10, or by a recruiter looking at commit
history as part of the portfolio evaluation this whole project is partly for, per
PRD §1).

## 5. Tagging & Phase Milestones

**Git tag at each phase's exit criterion being met** (per PRD §11's per-phase exit
criteria): `phase-0-complete`, `phase-1-complete`, etc. This serves two real purposes,
not just bookkeeping: a concrete rollback/reference point if something in a later
phase needs comparing against "the last known-good state before this phase started,"
and a legible narrative for anyone (including a recruiter) looking at the repo's tag
history to see the project's actual build progression over the 12 months — which is
a more credible signal than a README claiming "built in 12 months" with no evidence
in the history itself.

## 6. Local Guardrails — Pre-commit Hooks

**Husky + lint-staged** (frontend) running ESLint/Prettier on staged files before a
commit is even created — catches formatting/lint issues in seconds locally instead of
waiting for CI to fail minutes later. Cheap to set up, genuinely faster feedback loop,
not over-engineering — this is the kind of guardrail that pays for itself within the
first week of use.

## 7. What Never Gets Committed

`.gitignore` covers: `.env*` files, `node_modules/`, build output (`target/`, `dist/`),
IDE-specific files. Restated explicitly even though it's an obvious rule, because
`12_Security.md` §9's entire secrets-management design depends on it never being
violated even once — a secret committed to git history is compromised permanently
(rotating the secret afterward doesn't undo the exposure, it just stops future
damage), which is a stronger statement than "please don't do this."

---

## Next Document

`17_Coding_Standards.md` — naming conventions, formatting rules, and language-specific
style guides (Java/Spring, TypeScript/React) that the pre-commit hooks in §6 and the
CI lint step (`14_DevOps.md` §3) actually enforce.

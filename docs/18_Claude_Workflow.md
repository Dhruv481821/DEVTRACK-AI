# DevTrack AI — Claude Workflow

**Status:** Draft v1.0
**Depends on:** the entire `02`–`17` document set
**Purpose:** this is the one document in the set that isn't about the product — it's
about how you and Claude actually use the other 19 documents once implementation
starts, so the discipline this planning phase has followed doesn't quietly evaporate
the moment real code gets written under time pressure.

---

## 1. The Core Rule: Never Ask for a Whole Module in One Shot

Directly extending your own original instruction ("never generate the whole project
in a single response... always build professionally in phases"). Concretely, this
means:

- A single implementation request should map to **one module, at most one phase's
  worth of work**, not "build Phase 2." Even "build the DSA Tracker" is better split
  into schema → repository/service → controller → frontend query hooks → components,
  reviewed at each step, per `13_Testing.md`'s own layered structure.
- Before asking for code, name **which `FR-*` ID(s)** the request maps to
  (`03_Software_Requirements_Specification.md`) — this is the same traceability
  discipline `16_Git_Workflow.md` §3 already requires for PRs; it should start at the
  prompt, not get retrofitted at PR time.

## 2. Before Writing Code for a New Phase: A Mini-Planning Pass

**Important, easy-to-miss gap:** this blueprint gave **full column-level detail only
for Phase 0's schema and endpoints** (`05_Database_Architecture.md` §7,
`06_API_Specification.md` §2). Phases 1–5's entities and endpoints exist only as
summary tables (`05_Database_Architecture.md` §8, `06_API_Specification.md` §3) —
by design, per the phased-documentation approach used throughout. **This means each
new phase needs its own short planning pass before implementation, not just a jump
to code:**

1. Extend `05_Database_Architecture.md` with that phase's full schema (columns,
   indexes, constraints) — append a new dated section, don't silently create tables
   that were never written down.
2. Extend `06_API_Specification.md` with that phase's endpoint detail.
3. Only then start implementation, following `07`/`08`'s established layering.

This is the same pattern this entire session followed for Phase 0 — it should repeat
per phase, not be assumed already done for Phases 1–5 just because they're named in
the PRD.

## 3. Keeping the Docs and the Code From Drifting Apart

A design doc that stops matching the real system is worse than no doc — it actively
misleads. Two concrete rules:

- **Any time an implementation decision genuinely changes something this blueprint
  already decided** (e.g., Bucket4j's in-memory mode turns out to have a problem and
  gets replaced with something else before Phase 2 even arrives) — **the relevant
  document gets updated in the same PR as the code change**, not left to drift. Add
  this as a literal line item to `16_Git_Workflow.md` §3's PR checklist if it isn't
  exercised naturally.
- **Update in place, but don't erase the reasoning.** If a decision changes, add a
  short dated note (`> **Update, <date>:** ...`) near the original decision rather
  than deleting it outright — the *why* something changed is worth as much as the
  *what*, especially for a portfolio artifact where the design reasoning is part of
  the point (PRD §5's "engineering-credibility track").

## 4. When a Future Session Disagrees With a Past Decision

This applies to Claude specifically, in a future conversation, possibly without full
memory of this one: **a documented decision in this blueprint is the default source
of truth.** If a future implementation session concludes a past decision was wrong,
the right behavior is the same one this session has used throughout — **state the
disagreement explicitly, explain the reasoning, and get confirmation before
overriding it** — not silently implement something different from what's documented
and leave the doc stale. This mirrors your own stated preference (never blindly
agree, challenge assumptions) applied in the other direction: a future Claude
session shouldn't blindly override *past* Claude/human decisions either, without
surfacing the disagreement the same way this session surfaced pushback on the
original prompt's gaps.

## 5. Progress Tracking — One Place, Not a New Document

**Decision: phase/FR-ID progress is tracked in `19_Development_Roadmap.md`
(written next), not a separate `STATUS.md` or project-management doc.** Creating a
second tracking surface risks the exact drift problem §3 exists to prevent — two
places that can disagree about what's actually done. The roadmap doc already needs
to break each phase down into concrete work items; that same structure is where
"done/in progress/not started" state belongs.

## 6. Context Management for Coding Sessions

Practical, not architectural: when starting a session to build a specific piece,
reference the **specific relevant document(s)**, not the entire 20-file blueprint.
Building the auth module needs `05` §7, `06` §2.1, `07` (all), and `12` §2–3
specifically — pointing to exactly those keeps the session focused on what's actually
being built, rather than re-deriving context from the whole doc set every time.

---

## Next Document

`19_Development_Roadmap.md` — the concrete, sprint-level breakdown of Phase 0
specifically (per this doc's own §2 rule: detail the phase you're about to build, not
all six at once), with each work item traced to its `FR-*` ID, ready to actually start
implementation against.

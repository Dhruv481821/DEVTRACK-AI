# DevTrack AI — Testing Strategy

**Status:** Draft v1.0
**Depends on:** `12_Security.md`
**Feeds into:** `14_DevOps.md` (CI pipeline), `19_Development_Roadmap.md`

Every prior document has referenced "critical-path coverage" as a requirement without
defining what it actually means. This document makes it concrete: a specific,
enumerated list per module, plus what level of testing everything *else* gets — so
"test the important stuff" becomes an actual, checkable list rather than a vibe.

---

## 1. Testing Philosophy

Standard pyramid, sized for a solo 12-month build — **most tests are fast unit tests,
a smaller set of integration tests verify layers actually connect correctly, and a
deliberately small set of true end-to-end tests cover only the handful of journeys
where a bug would be genuinely embarrassing to ship.** This isn't a compromise on
quality — an exhaustive e2e suite for a solo dev is real ongoing maintenance burden
(brittle selectors, slow CI) for marginal benefit over well-targeted unit/integration
tests, which is exactly the kind of over-engineering your own rules warn against.

**Coverage target: no blanket percentage gate.** A codebase can hit 90% line coverage
while never testing the one function that actually matters (refresh token rotation)
and instead covering a hundred trivial getters. **Decision: the enumerated critical
paths in §4 require tests and are CI-gated (a PR touching that code without
corresponding test changes should be a visible review flag, not necessarily a hard
block for a solo repo with no other reviewer). Everything else gets tests where they
add real confidence, with no minimum percentage enforced.**

## 2. Backend Testing Layers

| Layer | Tool | What it verifies |
|---|---|---|
| Unit (service layer) | JUnit 5 + Mockito | Business logic in isolation — repositories, `EmailService`, `AiProvider`, GitHub client all mocked via their interfaces (the interface-based design from `07_Backend_Architecture.md` §1 exists partly *for* this — swappable in production, mockable in tests, same reason) |
| Integration (repository) | JUnit 5 + **Testcontainers (real Postgres)**, not H2 | Actual query behavior against the real engine — H2 doesn't faithfully reproduce Postgres-specific behavior (UUID generation, `jsonb` columns, exact constraint/index behavior from `05_Database_Architecture.md`), so a repository test passing against H2 and failing against real Postgres in production is a real, known failure mode this avoids entirely by testing against the real thing from the start |
| Integration (controller) | `MockMvc` / `WebTestClient` | Request → response envelope shape (`06_API_Specification.md` §1.3), status code mapping (§1.4), validation error format — verifies the *contract*, not just that a 200 comes back |
| Security | JUnit 5 | JWT validation/expiry, `@PreAuthorize` RBAC enforcement actually denies unauthorized roles, rate-limit buckets actually reject over-limit requests |

## 3. Frontend Testing Layers

| Layer | Tool | What it verifies |
|---|---|---|
| Component | Vitest + React Testing Library | User-visible behavior (what renders, what happens on interaction) — never implementation detail (internal state, which specific function fired). RTL's own philosophy enforces this by design; worth stating as a rule anyway since it's easy to drift toward brittle tests under deadline pressure |
| Hooks | Vitest + RTL's `renderHook`, wrapped in a test `QueryClientProvider` | Custom TanStack Query hooks (§08 architecture) behave correctly against mocked API responses |
| API layer | **MSW (Mock Service Worker)**, not manually mocking `fetch` | Intercepts at the network level, so tests exercise the real `apiClient` code (envelope unwrapping, the 401→refresh→retry flow from `08_Frontend_Architecture.md` §4) against realistic HTTP responses, rather than mocking `fetch` directly and silently skipping the code that processes its output |
| End-to-end | **Playwright, deliberately small scope (§5)** | The handful of true critical journeys, browser-real |

## 4. Critical-Path Coverage — The Concrete List

This is the list every prior document's "critical-path coverage" requirement actually
means. Required, tested, and specifically called out in PR review against this list:

**Auth (`FR-AUTH-*`)**
- Registration with valid/invalid input
- Login success/failure (wrong password, unknown email — same generic error message
  for both, to avoid user enumeration via error message differences, a detail worth
  testing explicitly since it's easy to accidentally leak)
- **Refresh token rotation and reuse-detection** (`12_Security.md` §2.4) — this is
  arguably the single most important test in the whole suite; a bug here is a session-
  security bug
- Password reset end-to-end
- RBAC denial (a non-owner/wrong-role request is actually rejected, not just "the
  happy path returns 200")

**Ownership enforcement (`06_API_Specification.md` §1.6)**
- One shared test suite against `assertOwnership()` (`07_Backend_Architecture.md`
  §4) — verifying it throws `ResourceNotFoundException` (404), never a 403, across
  every resource type that uses it. Testing this once, at the shared-helper level,
  covers the guarantee for every module that calls it — not re-tested per module.

**Resume Analyzer (`FR-ATS-01`)**
- Scoring logic against known input/expected-output fixtures — this is the kind of
  logic that's easy to silently regress while refactoring, and it's a core
  differentiating feature (PRD §1), so it needs real regression protection.

**GitHub sync (`FR-GH-02`)**
- Data mapping/parsing logic from GitHub's API response shape into DevTrack's
  internal model, tested against realistic fixture payloads (not live GitHub API
  calls — see §6).

**AI Service Layer (`09_AI_Architecture.md`)**
- Prompt assembly correctly separates system/user-data/output-schema sections
  (§4's structural injection mitigation) — a test that asserts the delimiter
  structure is actually present, not just "trust the code."
- Cache key generation (§6) produces the same key for unchanged source data and a
  different key after a relevant mutation — this is the test that actually proves
  the caching/invalidation design works, not just that it compiles.
- Fallback behavior (§8) on a simulated provider failure/429 — the degraded-mode UX
  states are real code paths, and they're exactly the kind of code that only runs
  during an outage, meaning it's untested-by-default unless deliberately covered here.

**Public portfolio endpoint (`FR-PORT-02`)**
- A test that asserts the response DTO (`PortfolioPublicView`,
  `04_System_Architecture.md` §6) contains *only* allowlisted fields — this is the
  concrete, automated version of the "don't accidentally leak private data publicly"
  rule; a test enforces it instead of relying on every future PR author remembering
  the rule.

## 5. End-to-End Scope (deliberately small)

Only these journeys get true browser e2e coverage — chosen because each spans
multiple layers (frontend + backend + DB) in a way unit/integration tests can't fully
verify, and each would be genuinely embarrassing to ship broken:

1. Register → verify email → login → see dashboard
2. Connect GitHub → see synced data appear
3. Build a resume → run ATS analysis → see a score
4. Create a job application → move it through stages

**Explicitly not e2e-tested:** every CRUD variation of every module, every form
validation message, every UI state — those are unit/integration-covered. Four e2e
tests is a deliberate ceiling for v1, not a starting point to be quietly expanded
module by module until the suite is slow and brittle.

## 6. Mocking External Providers

**No test — unit, integration, or e2e — ever calls a real external service**
(GitHub API, Gemini, Resend, Cloudinary). Every external integration is accessed
through an interface (`GithubClient`, `AiProvider`, `EmailService`, already
interface-based per `07_Backend_Architecture.md` §1), and tests substitute a fake/mock
implementation. Realistic fixture payloads (captured example responses) back these
mocks so tests reflect real API response shapes, not idealized ones — a mock that
only returns clean, minimal data doesn't catch bugs that real messy API responses
would.

## 7. Test Data

Builder/factory pattern for entities (e.g., `UserTestDataBuilder.aUser().withEmail(...)`)
rather than hardcoded fixture objects scattered across test files — one place to
update when an entity's required fields change, per the same DRY principle applied to
production code. Repository/integration tests run against a fresh Testcontainers
Postgres instance per test class (not a shared persistent test DB), so tests are
isolated and order-independent.

## 8. What Deliberately Isn't Tested

Stated explicitly, since "what not to test" is as much a decision as what to test:

- MapStruct-generated mappers — compile-time generated and verified by the build
  itself (`07_Backend_Architecture.md` §2); a runtime test re-verifying generated
  code adds no real confidence.
- shadcn/Radix primitive internals (focus trapping, ARIA behavior) — that's tested
  upstream by Radix; DevTrack's tests should verify *usage* of these primitives, not
  re-prove their own correctness.
- Trivial getters/setters/DTOs with no logic.

---

## Next Document

`14_DevOps.md` — CI pipeline (test execution from this doc, Dependabot per
`12_Security.md` §12), Docker setup, and the environment promotion strategy
(dev → staging → prod).

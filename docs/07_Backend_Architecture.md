# DevTrack AI — Backend Architecture

**Status:** Draft v1.0
**Depends on:** `06_API_Specification.md`
**Feeds into:** `08_Frontend_Architecture.md`, `09_AI_Architecture.md`, `12_Security.md`, `13_Testing.md`

This document closes the one open item carried over from the API spec (Phase 0 rate
limiting) and defines the internal code structure every module follows — the goal is
that a new module (Phase 1 onward) is built by copying an established pattern, not by
re-deciding layering conventions each time.

---

## 1. Layering (per module, from `04_System_Architecture.md` §4's package boundaries)

Every module package follows the same internal shape:

```
com.devtrack.<module>
├── controller/     — REST controllers. Thin: validate input shape (via DTO + Bean
│                     Validation), delegate to service, return DTO. No business logic.
├── service/        — Business logic lives here. Interfaces + implementations where
│                     the implementation is genuinely swappable (EmailService,
│                     AiProvider); concrete classes otherwise — an interface with
│                     exactly one implementation and no swap scenario is ceremony,
│                     not architecture.
├── repository/     — Spring Data JPA repositories. No business logic — query methods
│                     and `@Query` only.
├── entity/         — JPA entities. Never returned directly from a controller (see §2).
├── dto/
│   ├── request/    — Input DTOs, annotated with Bean Validation constraints.
│   └── response/   — Output DTOs, shaped exactly as the API spec defines, not as a
│                     1:1 entity mirror.
├── mapper/         — MapStruct interfaces, entity ↔ DTO.
└── exception/      — Module-specific exceptions, extending the shared hierarchy (§4).
```

**Rule:** `controller` depends on `service`, `service` depends on `repository` and
other modules' `service` interfaces *only* where §4 of `04_System_Architecture.md`
explicitly allows it (the `ai/` module reading across modules; event listeners in
`calendar/`/`notifications/`). No layer is ever skipped (a controller never calls a
repository directly), and no module reaches into another module's `repository` or
`entity` package — cross-module data access goes through that module's `service`
interface, keeping the module boundary real in code, not just in the diagram.

## 2. DTO & Mapping Strategy

- **Entities never leave the service layer.** Every controller method returns a
  response DTO, mapped via MapStruct. This isn't just style — it's what makes the
  Portfolio CMS's allowlist-DTO security rule (`04_System_Architecture.md` §6)
  actually enforceable: if entities never reached a controller in the first place,
  "accidentally serializing a private field" isn't a mistake that's even reachable in
  the public portfolio path, because the public endpoint's mapper only knows how to
  produce `PortfolioPublicView` from allowlisted fields, full stop.
- **MapStruct**, not manual mapping or a generic reflection-based mapper — compile-time
  generated, so a mapping mismatch is a build failure, not a runtime surprise.
- Request DTOs are validated with Jakarta Bean Validation annotations
  (`@NotBlank`, `@Email`, `@Size`, custom validators for domain rules like password
  complexity). This is the server-side validation the SRS (`NFR-SEC-01`) requires
  regardless of what the frontend's Zod schemas already check — frontend validation is
  UX, this is the actual security boundary.

## 3. Entity Design Notes (Lombok caution)

Worth stating explicitly, since it's a common real-world footgun: **entities do not
use `@Data`.** `@Data` generates `equals()`/`hashCode()`/`toString()` including
relationship fields, which causes two concrete problems with JPA: infinite recursion
on bidirectional relationships (`toString()` on `User` prints `Profile`, which prints
`User`, forever) and broken `equals()`/`hashCode()` semantics with lazy-loaded proxies
(a `LAZY` collection's `hashCode()` can trigger an unwanted DB fetch, or worse, differ
before/after the proxy is initialized).

**Decision:** entities use `@Getter`/`@Setter` explicitly (not `@Data`), `equals()`/
`hashCode()` based on the entity's `id` field only (with a null-check for
not-yet-persisted entities), and `@ToString` explicitly excludes relationship fields
(`@ToString.Exclude` or field-level omission).

## 4. Global Exception Handling

One `@RestControllerAdvice`, mapping a shared exception hierarchy to the error
envelope defined in `06_API_Specification.md` §1.3:

```
DevTrackException (abstract base, carries an ErrorCode enum)
├── ResourceNotFoundException      → 404, RESOURCE_NOT_FOUND
├── ValidationException             → 400, VALIDATION_ERROR
├── AuthenticationException          → 401, AUTH_INVALID_CREDENTIALS / AUTH_TOKEN_EXPIRED
├── AuthorizationException            → 403, FORBIDDEN
├── ConflictException                  → 409, CONFLICT (e.g., duplicate email)
├── RateLimitExceededException          → 429, RATE_LIMITED
└── AiQuotaExceededException             → 429, AI_QUOTA_EXCEEDED
```

Unhandled exceptions (anything not in this hierarchy — a genuine bug, not an expected
business condition) are caught by a final catch-all handler: logged with a correlation
ID (§7), returned as a generic 500 with no stack trace or internal detail in the
response body (`06_API_Specification.md` §1.4's explicit rule).

**Ownership-check convention (`06_API_Specification.md` §1.6) is implemented once,
here:** a shared service-layer helper (`assertOwnership(resource, currentUserId)`)
throws `ResourceNotFoundException` — not a separate "not owned" exception — precisely
so the 404-not-403 rule can't be accidentally violated by a module author who reaches
for a more "accurate"-sounding 403 exception instead.

## 5. Security Configuration

- Spring Security, **stateless** session policy (no server-side session state — JWT
  access token per request, refresh token cookie per `06_API_Specification.md` §2.1).
- One `JwtAuthenticationFilter` validates the access token and populates the security
  context; RBAC enforcement uses method-level `@PreAuthorize("hasRole('USER')")` —
  trivial today with one role, but the mechanism is real and tested from Phase 0, so
  adding a second role later (per `04_System_Architecture.md`'s "extensible interface,
  not a role-management system" decision) is additive, not a retrofit.
- CORS: explicit origin allowlist (the Vercel frontend URL, per env), not a wildcard —
  closes review finding 3.5.

## 6. Rate Limiting — Resolving the Phase 0 Gap

`06_API_Specification.md` §2.1 flagged that Redis (needed for distributed rate
limiting) doesn't exist until Phase 2, but Phase 0 ships the endpoints that need
limiting now.

**Decision: Bucket4j, in-memory backend for Phase 0, migrated to Bucket4j's
Redis-backed distributed mode in Phase 2 — same library, different backend, so the
migration is a configuration change, not a rewrite.**

- Phase 0: `Bucket4j` with an in-memory `ProxyManager`, safe under the single-instance
  constraint already established (`04_System_Architecture.md` §8). Applied to
  `/auth/login` and `/auth/password-reset/request` specifically (`NFR-SEC-02`'s
  stricter-limit requirement).
- Phase 2: swap to `Bucket4j`'s Redis-backed `ProxyManager`, extending the same rate
  limits to the AI fair-use policy (`04_System_Architecture.md` §3.2) and any future
  multi-instance deployment.
- Exceeding a limit throws `RateLimitExceededException` (§4), returning `429` with
  a `Retry-After` header.

## 7. Observability Baseline

- Every request gets a correlation ID (via a servlet filter, propagated into MDC for
  logging) — referenced in §4's 500-handling and useful the moment this app has more
  than one concurrent user to debug issues for.
- Structured JSON logging (not plain text) from day one — trivial to add now, painful
  to retrofit once log volume exists.
- Full metrics/tracing stack (e.g., dashboards, alerting) is out of scope for this doc
  — belongs in `14_DevOps.md` once deployment topology is finalized there.

## 8. Transaction Boundaries

`@Transactional` at the service layer method level, never at the controller or
repository layer — a controller method that happens to call two service methods
should not implicitly share a transaction it didn't ask for. Read-only queries are
annotated `@Transactional(readOnly = true)` (a real optimization with Hibernate, not
just documentation — it skips dirty-checking overhead).

## 9. Configuration & Secrets

- `application.yml` per Spring profile (`dev`, `staging`, `prod`), committed to the
  repo for non-secret config (timeouts, feature flags).
- Secrets (DB credentials, Gemini API key, Resend API key, JWT signing key, Cloudinary
  credentials) come from environment variables only, injected by Railway/Vercel's
  environment configuration — never committed, never in `application.yml` directly.
  Full secrets-handling detail belongs in `12_Security.md`.

---

## Next Document

`08_Frontend_Architecture.md` — React project structure, TanStack Query + Zustand
boundaries (per your original state-management split), the API client layer that
consumes this backend's response envelope (§2 of this doc), and how the frontend
handles the 401 → refresh-token → retry flow against `06_API_Specification.md`'s auth
transport decision.

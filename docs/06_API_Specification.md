# DevTrack AI — API Specification

**Status:** Draft v1.0
**Depends on:** `05_Database_Architecture.md`
**Feeds into:** `07_Backend_Architecture.md`, `08_Frontend_Architecture.md`

Same phased approach as the DB doc: conventions that apply system-wide are fully
decided here, then full endpoint-level detail for **Phase 0 only**. Later phases get
their endpoints designed when their phase starts — listed at a summary level now for
traceability.

---

## 1. Conventions

### 1.1 Versioning

**Decision: URI versioning — `/api/v1/...`.**

*Why, not just what:* header-based or content-negotiation versioning is arguably more
"correct" REST, but URI versioning is unambiguous in logs, trivially testable with
curl, and visible in every request without inspecting headers — for a solo dev who is
both the API author and its only consumer for most of this timeline, debuggability
wins over textbook purity. `v1` is assumed stable for the life of this product;
breaking changes get `v2`, additive changes (new optional fields) do not bump version.

### 1.2 Resource naming

Plural nouns, kebab-case for multi-word resources: `/api/v1/job-applications`, not
`/api/v1/jobApplication`. This is a deliberate difference from the DB's singular
`snake_case` table names (`05_Database_Architecture.md` §2) — REST URL convention and
SQL naming convention are different domains with different idiomatic norms, and
forcing them to match either would violate one convention to satisfy the other.

### 1.3 Response envelope

Every response — success or error — uses the same top-level shape, so frontend code
never has to branch on "is this an error-shaped or success-shaped object":

```json
// Success
{
  "success": true,
  "data": { ... },
  "meta": { "timestamp": "2026-08-07T10:00:00Z" }
}

// Success (paginated list)
{
  "success": true,
  "data": [ ... ],
  "meta": {
    "timestamp": "2026-08-07T10:00:00Z",
    "pagination": { "page": 0, "size": 20, "totalElements": 143, "totalPages": 8 }
  }
}

// Error
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Password must be at least 8 characters and contain a number.",
    "details": [ { "field": "password", "reason": "too_short" } ]
  },
  "meta": { "timestamp": "2026-08-07T10:00:00Z" }
}
```

`error.code` is a stable, machine-readable string (`VALIDATION_ERROR`,
`AUTH_INVALID_CREDENTIALS`, `AUTH_TOKEN_EXPIRED`, `RESOURCE_NOT_FOUND`,
`RATE_LIMITED`, `AI_QUOTA_EXCEEDED`, ...) — the frontend should never need to pattern-
match on `error.message` text to decide behavior; `error.message` is for humans,
`error.code` is for code.

### 1.4 HTTP status codes

Standard mapping, enforced by a **global exception handler** (`@RestControllerAdvice`)
so no individual controller hand-rolls status codes:

| Status | Meaning |
|---|---|
| 200 | Success (GET, successful PUT/PATCH) |
| 201 | Resource created (POST) |
| 204 | Success, no body (DELETE) |
| 400 | Validation error |
| 401 | Missing/invalid/expired auth token |
| 403 | Authenticated but not authorized (RBAC denial) |
| 404 | Resource not found (or not owned by the requesting user — see §1.6) |
| 409 | Conflict (e.g., email already registered) |
| 429 | Rate limited (`NFR-SEC-02`, or `AI_QUOTA_EXCEEDED` per `04_System_Architecture.md` §3.2) |
| 500 | Unhandled server error — logged with a correlation ID, never leaks stack traces to the client |

### 1.5 Pagination, filtering, sorting

Query params on all list endpoints: `?page=0&size=20&sort=createdAt,desc`. Filtering is
resource-specific (e.g., `GET /api/v1/job-applications?stage=INTERVIEW`) and documented
per-endpoint when that phase's spec is written — no generic filter DSL for v1, that's
solving a flexibility problem this product doesn't have yet.

### 1.6 Ownership enforcement

Every resource in this system belongs to exactly one user (no shared/team resources in
v1, per PRD non-goals). **Convention: a resource that exists but isn't owned by the
requesting user returns 404, not 403.** Returning 403 confirms the resource exists
(an enumeration leak); 404 doesn't. This is a security-by-default convention applied
uniformly, not decided per-endpoint.

### 1.7 Idempotency

`POST` endpoints that create a resource as a side effect of an otherwise-repeatable
action (e.g., "resend verification email") are designed to be safely retryable by the
client without server-side idempotency keys for v1 — genuine idempotency-key
infrastructure is more machinery than a single-user-per-request system needs right
now. Flagged as a deliberate v1 simplification, not an oversight.

### 1.8 Documentation

Swagger/OpenAPI generated from annotated controllers (springdoc-openapi), available at
`/api/v1/docs` in non-production environments. This is generated from code, not
hand-maintained separately — a hand-written API doc that drifts from the real
implementation is worse than no doc.

---

## 2. Phase 0 Endpoints

### 2.1 Auth (`auth` module)

| Method | Path | Purpose | FR |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Email/password registration | `FR-AUTH-01` |
| POST | `/api/v1/auth/login` | Email/password login → access + refresh token | `FR-AUTH-02` |
| GET | `/api/v1/auth/google` | Redirect to Google OAuth consent | `FR-AUTH-02` |
| GET | `/api/v1/auth/google/callback` | OAuth callback, issues token pair | `FR-AUTH-02` |
| POST | `/api/v1/auth/refresh` | Exchange refresh token for new pair (rotates, per `05_Database_Architecture.md` §7) | `FR-AUTH-03` |
| POST | `/api/v1/auth/logout` | Revokes current refresh token server-side | `FR-AUTH-04` |
| POST | `/api/v1/auth/password-reset/request` | Sends reset email via Resend | `FR-AUTH-05` |
| POST | `/api/v1/auth/password-reset/confirm` | Consumes reset token, sets new password | `FR-AUTH-05` |
| POST | `/api/v1/auth/verify-email` | Consumes email verification token | (supports `email_verified` field, §7 of DB doc) |

**Auth transport decision:** access token returned in the JSON response body (frontend
holds it in memory, not localStorage — avoids XSS-exfiltration of the access token).
Refresh token set as an `httpOnly`, `secure`, `SameSite=Strict` cookie (per SRS
`NFR-SEC-04`) — never exposed to JS at all, which is the actual point of making it
httpOnly.

**Rate limiting:** `login` and `password-reset/request` are rate-limited more strictly
than other endpoints (`NFR-SEC-02`), using the Redis-backed sliding window from
`05_Database_Architecture.md` §3 once Redis lands in Phase 2. **Gap, stated
explicitly:** Phase 0 ships before Redis exists (Phase 2) — so Phase 0's auth rate
limiting needs an in-memory or DB-backed fallback (e.g., a simple counter table or
Bucket4j in-memory) until Phase 2, or the rate-limit requirement is technically unmet
for two phases. Decide at `07_Backend_Architecture.md`: recommend a lightweight
in-memory rate limiter (Bucket4j, single-instance-safe per `04_System_Architecture.md`
§8's single-instance constraint) for Phase 0, migrated to Redis-backed in Phase 2 —
cheap now, upgraded later, not skipped.

### 2.2 Profile & Settings (`profile` module)

| Method | Path | Purpose | FR |
|---|---|---|---|
| GET | `/api/v1/profile/me` | Current user's profile | `FR-PROF-01` |
| PATCH | `/api/v1/profile/me` | Update display name, bio | `FR-PROF-01` |
| POST | `/api/v1/profile/me/avatar` | Upload avatar (proxies to Cloudinary, returns URL) | `FR-PROF-01` |
| GET | `/api/v1/settings/me` | Current user's settings | `FR-SET-01` |
| PATCH | `/api/v1/settings/me` | Update theme, notification prefs | `FR-SET-01` |

### 2.3 Notifications (`notifications` module — infra only, per `04_System_Architecture.md` §3.5)

| Method | Path | Purpose | FR |
|---|---|---|---|
| GET | `/api/v1/notifications` | List current user's notifications, paginated | `FR-NOTIF-01` |
| PATCH | `/api/v1/notifications/{id}/read` | Mark one notification read | `FR-NOTIF-01` |
| PATCH | `/api/v1/notifications/read-all` | Mark all read | `FR-NOTIF-01` |

No producer endpoints — notifications are created internally by event listeners
(`04_System_Architecture.md` §5), never directly via a client-facing POST endpoint.

---

## 3. Later-Phase Endpoints (summary — full spec written per-phase)

| Phase | Resource root |
|---|---|
| 1 | `/api/v1/notes`, `/api/v1/events`, `/api/v1/study-plans` |
| 2 | `/api/v1/github`, `/api/v1/dsa-problems`, `/api/v1/certificates` |
| 3 | `/api/v1/resumes`, `/api/v1/resumes/{id}/analysis`, `/api/v1/job-applications`, `/api/v1/job-applications/{id}/interview-rounds` |
| 4 | `/api/v1/projects`, `/api/v1/portfolio` (public, unauthenticated — see `04_System_Architecture.md` §6 for the allowlist-DTO rule) |
| 5 | `/api/v1/ai/assistant`, `/api/v1/analytics/readiness` |

**One forward-looking flag:** the public `/api/v1/portfolio/{username}` endpoint in
Phase 4 is the one route in this entire API that's intentionally unauthenticated. It
gets its own short section in `06_API_Specification.md`'s Phase 4 addendum when that
phase is designed — worth remembering now so it isn't accidentally routed through the
same auth middleware chain as everything else and either wrongly blocked or, worse,
wrongly exempted more broadly than intended.

---

## Next Document

`07_Backend_Architecture.md` — Spring Boot project structure, service/repository
layering, DTO/mapping strategy (MapStruct), the global exception handler design
(§1.4), and the Phase 0 rate-limiting fallback decision flagged in §2.1.

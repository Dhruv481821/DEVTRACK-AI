# DevTrack AI — Frontend Architecture

**Status:** Draft v1.0
**Depends on:** `07_Backend_Architecture.md`
**Feeds into:** `09_AI_Architecture.md`, `10_UI_UX_Design_System.md`, `11_Component_Library.md`

---

## 1. Project Structure (feature-based, mirrors backend module boundaries)

```
src/
├── app/                — Router setup, root providers (QueryClient, theme), route
│                          definitions, top-level error boundary
├── features/
│   ├── auth/
│   │   ├── api/          — TanStack Query hooks calling the auth endpoints
│   │   ├── components/    — LoginForm, RegisterForm, etc.
│   │   ├── store/          — Zustand auth store (access token, current user — see §2)
│   │   └── schemas/         — Zod schemas matching backend Bean Validation rules
│   ├── dashboard/
│   ├── notes/
│   ├── github/
│   ├── resume/
│   ├── ...                — one folder per backend module, same names, so a dev
│                             (you, in month 9, having forgotten month 2) can find the
│                             frontend code for a feature by the same name as its
│                             backend module.
├── shared/
│   ├── ui/                — shadcn/ui primitives + any custom design-system components
│   ├── api-client/          — the envelope-aware fetch wrapper (§3)
│   ├── hooks/                 — cross-feature custom hooks
│   └── utils/
└── types/
    └── api/                — generated types (§3.1) — never hand-edited
```

**Rule, mirroring the backend's module boundary:** a feature folder may import from
`shared/`, but not from another feature folder's internals — cross-feature UI
composition happens at the `app/` (page/route) level, which is allowed to import from
multiple features, the same way only the `ai/` backend module is allowed to read
across other modules.

## 2. State Management Boundaries

Per your original instruction ("Do NOT use Redux"), and made concrete here so
"server state vs. UI state" isn't a judgment call made differently per feature:

| Goes in TanStack Query | Goes in Zustand |
|---|---|
| Anything that came from the API — notes, resumes, job applications, GitHub data, AI responses | Theme, sidebar open/closed, command palette open/closed, active dialog, toast queue |
| The current user's profile/settings (fetched, cached, invalidated on mutation) | **The current access token** (§4) — not server data in the REST sense, but session state that must survive re-renders without a network round-trip |
| Anything with a natural cache-invalidation story (mutate → invalidate query key) | Anything with no server counterpart at all |

**Concrete anti-pattern this table exists to prevent:** copying a TanStack Query
result into a Zustand store "for convenience" (e.g., so another component can read it
without its own `useQuery` call). This creates two sources of truth for the same
data that can silently drift — any component that needs server data calls `useQuery`
with the same query key and gets the shared cache for free. That's the entire point
of TanStack Query; routing server data through Zustand defeats it.

## 3. API Client Layer

**Decision: generate TypeScript types from the backend's OpenAPI spec
(`openapi-typescript`), don't hand-write request/response interfaces.**

*Why:* `07_Backend_Architecture.md` already commits to springdoc-openapi generating a
live spec from annotated controllers. Hand-writing matching TypeScript interfaces on
the frontend creates exactly the kind of two-sources-of-truth drift §2 warns against,
except across the network boundary instead of within the frontend — a backend DTO
field rename becomes a silent frontend bug instead of a build failure. Generated types
make it a build failure, which is the correct failure mode.

- A single `apiClient` wrapper (thin `fetch`-based, not axios — one fewer dependency,
  and `fetch` is sufficient for this app's needs) unwraps the response envelope
  (`06_API_Specification.md` §1.3): callers get `data` directly on success, or a typed
  `ApiError` thrown on failure (constructed from the envelope's `error.code`/`message`).
- TanStack Query hooks (`useNotes()`, `useResumes()`, etc.) wrap `apiClient` calls —
  components never call `apiClient` directly, they call a feature's query/mutation
  hooks, keeping query-key management centralized per feature.

## 4. Auth Token Handling & the Refresh Flow

Directly implements `06_API_Specification.md` §2.1's transport decision:

- **Access token:** held in a Zustand store, **in memory only — never `localStorage`
  or `sessionStorage`.** This is the XSS-mitigation half of the design: an injected
  script can't read a token that was never written to any browser storage API.
- **Refresh token:** never touched by frontend JS at all — it's an `httpOnly` cookie,
  sent automatically by the browser to `/api/v1/auth/refresh`.
- **Refresh-on-401 flow**, implemented once in `apiClient`, not per-feature:
  1. Any request returning 401 triggers a call to `/auth/refresh` (cookie sent
     automatically).
  2. **Single-flight guard:** if multiple requests 401 concurrently (e.g., a page
     firing 4 parallel queries right as the access token expires), only the *first*
     triggers a refresh call — the others await that same in-flight promise instead of
     each independently hitting `/auth/refresh` (which would race against the
     backend's refresh-token rotation, `05_Database_Architecture.md` §7, and cause the
     losing requests to fail against an already-rotated token).
  3. On successful refresh, the new access token is stored, and all queued requests
     (the original 401'd one plus anything that queued behind it) retry once.
  4. If refresh itself fails (refresh token expired/revoked), the auth store is
     cleared and the user is redirected to login — no infinite retry loop.

## 5. Routing

React Router, with route-based code splitting (`React.lazy` per top-level route) —
directly serves `NFR-PERF-01`'s Lighthouse target by keeping the initial bundle to
just the landing/auth routes, not the entire dashboard.

- **Public routes:** landing page, login, register, and the Phase 4 public portfolio
  route (`/u/:username`) — see §7 for a specific gap on that last one.
- **Protected routes:** wrapped in a single `<RequireAuth>` layout route that checks
  the Zustand auth store and redirects to login if empty — not repeated per page.

## 6. Forms

React Hook Form + Zod, per your original stack choice. **Convention:** each feature's
Zod schema is the single source of truth for client-side validation *shape*, but
mirrors (not replaces) the backend's Bean Validation rules
(`07_Backend_Architecture.md` §2) — client validation is UX (instant feedback),
server validation is the actual boundary. When a `VALIDATION_ERROR` response's
`error.details` array (`06_API_Specification.md` §1.3) returns field-level errors the
client didn't catch (a rule that only exists server-side, or a race like "email
already taken"), those map directly onto the same React Hook Form field-error state —
one error-display code path handles both client- and server-caught validation, not two.

## 7. Known Gap: Public Portfolio Page Has No Server-Side Rendering

Worth flagging explicitly rather than discovering it in Phase 4. This is a Vite SPA,
not Next.js — there's no SSR/static-generation story for any route, including the
Phase 4 public portfolio page (`/u/:username`). Two real consequences:

- **SEO:** search engine crawlers that don't execute JS see an empty shell, not the
  portfolio content.
- **Link previews:** sharing a portfolio URL on LinkedIn/Twitter/Slack won't produce a
  rich preview card (Open Graph tags need to be present in the initial HTML response,
  which a client-rendered SPA doesn't provide per-user).

For a feature whose entire purpose is "something to share with recruiters," weak link
previews is a real product gap, not a cosmetic one. **Decision for v1: accept this
gap**, since fixing it properly means either introducing SSR (a framework change with
much wider blast radius than one feature) or a bot-detection prerendering hack (real
complexity for a single route). **Flagged as a concrete Phase 4 backlog item** —
worth revisiting specifically then with options like a lightweight prerender-on-request
Vercel Edge Function serving OG tags to known crawler user agents, once it's clear the
feature is actually being used and shared.

## 8. Accessibility & Performance Baseline

- Focus is programmatically moved to the main heading on route change (a common SPA
  accessibility miss — screen reader users otherwise get no cue that navigation
  happened).
- Long lists (job applications, DSA log — `NFR-PERF-03`) use virtualization
  (`@tanstack/react-virtual`, consistent with the existing TanStack dependency rather
  than adding a second virtualization library).
- Images (avatars, certificate thumbnails) lazy-load and use explicit width/height to
  avoid layout shift (a direct Lighthouse CLS factor).

---

## Next Document

`09_AI_Architecture.md` — the AI Service Layer's actual design: prompt strategy per
agent configuration (`04_System_Architecture.md` §7's "one service, six configs"
decision), context-window budgeting (review finding 4.1), the prompt-injection
mitigation detail (review finding 4.4, architecturally constrained in
`04_System_Architecture.md` §3.4), and the caching/fair-use integration with Redis
(`05_Database_Architecture.md` §3).

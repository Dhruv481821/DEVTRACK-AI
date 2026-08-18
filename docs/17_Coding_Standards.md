# DevTrack AI — Coding Standards

**Status:** Draft v1.0
**Depends on:** `16_Git_Workflow.md`
**Enforced by:** pre-commit hooks (`16_Git_Workflow.md` §6) + CI lint step
(`14_DevOps.md` §3) — every rule below is either tool-enforced or it doesn't belong in
this document. A style rule nobody/nothing checks is a suggestion, not a standard.

---

## 1. General Principle

Consistency over personal preference, and **tool-enforced over reviewer-enforced** —
there's no second reviewer (`16_Git_Workflow.md` §3) to catch a formatting drift by
eye, so every rule here that can be automated, is. The handful that can't (naming
judgment calls, comment quality) are the only ones that rely on discipline rather than
tooling — kept deliberately short for that reason.

## 2. Backend (Java / Spring Boot)

**Formatter: Spotless, Google Java Format style** — applied automatically on commit
(Maven/Gradle Spotless plugin, wired into the pre-commit hook) and re-checked in CI
as a hard gate, not just a local nicety. No formatting debate is possible because no
formatting decision is manual.

**Naming:**
| Element | Convention | Example |
|---|---|---|
| Class | PascalCase, suffixed by role | `ResumeService`, `JobApplicationController`, `GithubSyncException` |
| Interface vs. impl | Interface named plainly (`AiProvider`), impl suffixed `Impl` **only** where an interface exists per `07_Backend_Architecture.md` §1's "interface only where genuinely swappable" rule — a service with no swap scenario is a concrete class, not `FooService`/`FooServiceImpl` ceremony | `GeminiAiProvider implements AiProvider` |
| DTOs | Suffixed by direction | `CreateResumeRequest`, `ResumeResponse` |
| Methods, variables | camelCase | `findActiveByUserId` |
| Constants | `UPPER_SNAKE_CASE`, `static final` | `MAX_AI_REQUESTS_PER_DAY` |
| Packages | lowercase, matching the module structure from `07_Backend_Architecture.md` §1 exactly | `com.devtrack.resume.service` |

**Dependency injection: constructor injection via `@RequiredArgsConstructor`
(Lombok), never field injection (`@Autowired` on a field).** Field injection makes a
class's dependencies invisible from its signature and untestable without reflection
tricks — constructor injection makes every dependency an explicit, visible,
constructor-testable parameter. This is a settled, non-debatable Spring convention at
this point, not a matter of taste.

**Null-safety:** `Optional<T>` as a **return type** for "may legitimately be absent"
service methods — never as a field type or a method parameter type (the well-known
anti-pattern of `Optional` fields/params adds serialization and equality complexity
for no real benefit over a plain nullable field with a clear contract). Service
methods never silently return `null` where `Optional.empty()` or a thrown
`ResourceNotFoundException` (`07_Backend_Architecture.md` §4) is the honest contract.

**Entity conventions:** per `07_Backend_Architecture.md` §3 — no `@Data`, explicit
`@Getter`/`@Setter`, ID-only equality, relationship fields excluded from `@ToString`.
Restated here as a coding standard, not just an architecture note, since it's exactly
the kind of rule a new file can violate by default if Lombok's `@Data` is reached for
out of habit.

**Javadoc:** required on public service **interfaces** (the contract a caller relies
on) — not required on private methods, trivial getters, or DTOs whose fields are
self-explanatory from their names and validation annotations.

**Formatting specifics:** 120-character line length, no wildcard imports, static
imports grouped separately from regular imports — all enforced by Spotless, none of
it a matter of remembering to do it by hand.

## 3. Frontend (TypeScript / React)

**Formatter: Prettier** (single quotes, semicolons, 100-character width) +
**ESLint** (`typescript-eslint` strict config + `eslint-plugin-react-hooks` +
**`eslint-plugin-jsx-a11y`**) — the accessibility linter is not optional tooling
here; it's the automated first line of defense for `NFR-A11Y-01`/`02`, catching
missing `alt` text, invalid ARIA usage, and non-interactive elements with click
handlers before a human ever has to notice them in review (which, again, isn't
happening — §1).

**TypeScript strictness:** `strict: true` in `tsconfig.json`, and
**`@typescript-eslint/no-explicit-any` set to `error`, not `warn`** — per your own
"Strict TypeScript" rule, a warning that never blocks a build is a suggestion; an
error that fails CI is a standard. `any` is only permitted with an inline
`// eslint-disable-next-line` and a comment explaining why (e.g., a genuinely
untyped third-party callback shape) — the exception must be visible and justified,
not silent.

**Component conventions:**
- Function components only — no class components anywhere in the codebase.
- **Named exports only, no default exports.** Default exports let a component be
  imported under a different name at every call site, which makes both IDE
  auto-import and a project-wide rename/refactor meaningfully harder to trust —
  named exports keep one canonical name for a component everywhere it's used. A
  small rule, but one of the few in this document that's a genuine judgment call
  rather than an industry-settled default, so it's stated with its reasoning rather
  than just asserted.
- Props typed via an explicit `interface ComponentNameProps` per component — never
  an inline object type for anything beyond one or two trivial fields.

**File naming:** PascalCase for component files (`ResumeCard.tsx`), camelCase for
hooks (`useResumeAnalysis.ts`) and utilities (`formatDate.ts`) — matching the
PascalCase/camelCase split already used for the things themselves (component names
vs. function names), not an arbitrary separate rule to memorize.

**Custom hooks:** must start with `use`, single responsibility — a hook named
`useResumeAnalysis` doesn't also manage unrelated dashboard-wide state; per
`08_Frontend_Architecture.md` §1's module-boundary rule, if a hook needs data from
two features, that's a signal that composition belongs at the `app/`/page level, not
inside the hook.

## 4. Cross-Cutting (both languages)

- **No commented-out code committed.** Git history is the record of what used to be
  there — a codebase shouldn't also try to be that record inline. Delete it; find it
  in `git log` if it's ever needed again.
- **No stray `console.log` / `System.out.println` in committed code.** Backend uses
  the structured JSON logger (`07_Backend_Architecture.md` §7); frontend debug output,
  if genuinely needed temporarily, is caught by an ESLint `no-console` rule (warn
  locally, error in CI) so it can't accidentally ship.
- **`TODO` comments must reference a concrete `FR-*` ID or a specific follow-up, never
  a vague `// TODO: fix this later`.** A TODO with no reference is functionally
  invisible the moment it's written — it won't be found again except by accident.

## 5. Enforcement Summary

| Rule category | Enforced by |
|---|---|
| Formatting (both languages) | Pre-commit hook (Spotless / Prettier), re-checked in CI |
| Lint rules (`no-explicit-any`, `no-console`, a11y, hooks rules) | ESLint in pre-commit + CI |
| Java naming/DI/null-safety conventions | Not fully tool-enforceable — relies on this document + the PR self-review checklist (`16_Git_Workflow.md` §3) |
| TypeScript naming/export conventions | Partially enforceable via custom ESLint rules; otherwise same as above |

The last row is the honest caveat: not everything in this document is a hard CI gate
— some of it is a written standard a solo developer holds themselves to, which is a
real limitation of not having a second reviewer, named here rather than implied.

---

## Next Document

`18_Claude_Workflow.md` — how this blueprint's documents are meant to be used
turn by turn when actually building each phase (referencing the right doc before
writing code, per your own "never skip planning" rule), and how to keep this doc set
itself from drifting out of sync with the real codebase as it's built.

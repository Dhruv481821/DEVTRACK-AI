# DevTrack AI — Component Library

**Status:** Draft v1.0
**Depends on:** `10_UI_UX_Design_System.md`
**Feeds into:** implementation directly — this is the last doc before Phase 0 frontend code

---

## 1. shadcn/ui Integration

shadcn/ui isn't a package dependency — it's generated components copied into the
codebase (Radix primitives + Tailwind), which is exactly why it fits this project:
**they become DevTrack's own components the moment they're generated**, styled with
the tokens from `10_UI_UX_Design_System.md`, not shadcn's default theme.

- Tailwind config maps CSS variables to the named tokens from doc 10 directly
  (`--color-void`, `--color-signal`, etc.) — shadcn's generated components reference
  these variables, so the whole library re-themes from one place if a token changes,
  rather than hunting through every component file.
- **Why shadcn over a component package (MUI, Chakra, Ant):** those ship their own
  visual identity that fights the "not a templated default" brief on sight — anyone
  who's used Material UI recognizes it instantly. shadcn ships no visual identity of
  its own; it's Radix's accessible behavior with Tailwind classes you fully own and
  restyle. That's the entire reason it was in the original tech stack choice, and it's
  the right one for this brief specifically.
- **Radix's built-in accessibility is not optional infrastructure to reproduce
  elsewhere** — focus trapping in dialogs, correct ARIA roles on tabs/menus/comboboxes,
  keyboard navigation on the command palette — comes for free from the primitive.
  Custom components (§4) that don't have a Radix equivalent need their accessibility
  behavior built deliberately, not assumed.

## 2. Component Inventory

### 2.1 Primitives (shadcn-derived, live in `shared/ui/`)

Button, Input, Textarea, Select, Checkbox, Radio, Switch, Badge, Avatar, Tooltip,
Dialog, Dropdown Menu, Tabs, Popover, Toast, Separator.

**Every interactive primitive defines its full state set explicitly** — default,
hover, focus-visible (using the `signal`-colored focus ring from doc 10 §7, never
suppressed), active/pressed, disabled, and loading where applicable (buttons that
trigger async actions show a loading state, not just a disabled one — a disabled
button with no explanation is a common real usability miss).

### 2.2 Layout

| Component | Purpose |
|---|---|
| `AppShell` | The persistent dashboard shell (`FR-DASH-01`) — sidebar + top bar + content area |
| `PageHeader` | Title + optional actions, consistent across every module page |
| `Card` | Solid-elevation container (doc 10 §4) — the default content container throughout |
| `GlassPanel` | Glass-elevation container (doc 10 §4) — command palette, AI panel, notification dropdown only |

### 2.3 Data Display

| Component | Purpose |
|---|---|
| `DataTable` | Built on `@tanstack/react-table` (sorting/filtering) + `@tanstack/react-virtual` (virtualization past 50 rows, per `NFR-PERF-03`) — one implementation, reused for DSA log, Job Tracker list, GitHub repo list, not rebuilt per module |
| `StatCard` | A single metric + trend, using the `mono` type token (doc 10 §2) for the number itself |
| `ContributionHeatmap` | GitHub-style activity grid (`FR-GH-03`) |
| `Kanban` | Column/card board — powers both Job Tracker (`FR-JOB-01`) and Project Management (`FR-PROJ-01`) as one shared component, since both are genuinely the same interaction pattern (cards moving between named stages) with different data behind them |
| `Skeleton` | Content-shaped loading placeholders (§5) |
| `EmptyState` | Icon + title + description + optional action slot (§4) |

### 2.4 Navigation

| Component | Purpose |
|---|---|
| `CommandPalette` | Built on `cmdk` (the library shadcn itself is commonly paired with for this) — global keyboard-triggered (`Cmd/Ctrl+K`) fuzzy search across pages and actions |
| `Sidebar` | Primary navigation, collapsible |
| `Breadcrumbs` | Used on nested detail pages (e.g., a single job application's detail view) |

### 2.5 Domain-Composed Components (live in their feature folder, not `shared/ui/`)

This is a deliberate boundary, not a missing category — per `08_Frontend_Architecture.md`
§1's rule that feature folders own their domain logic:

| Component | Lives in | Composed from |
|---|---|---|
| `ReadinessScoreCard` | `features/analytics/` | `StatCard` + `AIResponseCard` |
| `ResumeAtsScorePanel` | `features/resume/` | `Card` + custom score visualization |
| `AIResponseCard` | `shared/ui/` (exception — see below) | Implements `09_AI_Architecture.md` §8/§9 directly: renders the response, the "Based on: ..." sourcing footer, and the degraded-mode states (cached/unavailable/quota-exhausted) as one component every agent surface reuses, rather than each AI feature inventing its own loading/error/sourcing UI |

**Why `AIResponseCard` is the one domain-flavored component in `shared/ui/`:** every
one of the six AI agents (`09_AI_Architecture.md` §3) needs identical handling for the
same three states (fresh response, cached/stale, unavailable) and the same sourcing
footer. Building this once and reusing it across `features/resume/`,
`features/dashboard/`, etc. is what actually keeps `09_AI_Architecture.md` §8's
degraded-mode design consistent everywhere it's used — six independent
implementations would drift the moment one of them is updated and the other five
aren't.

## 3. Rich Text & Specialized Inputs

- **Notes editor (`FR-NOTES-01`):** a lightweight rich-text component (e.g., Tiptap)
  rather than a full document-editing suite — Notes is explicitly scoped narrower
  than Notion/Obsidian (PRD §4's competitive framing), and the editor should reflect
  that scope, not gold-plate a feature that isn't the product's core bet.
- **Resume Builder (`FR-RESUME-01`):** structured form fields (React Hook Form + Zod,
  per doc 08 §6), not a free-text/WYSIWYG editor — this was already decided at the
  requirements level (SRS `FR-RESUME-01`: "form-driven editor, not free-text") and is
  reiterated here so the component choice doesn't accidentally contradict it.

## 4. Empty States as a First-Class Pattern

Per the PRD's own product principle (§1: "empty states are a designed product
surface, not an afterthought"), `EmptyState` is not a fallback — it's used
deliberately everywhere a module has no data yet (a new user's Notes page, DSA log,
Job Tracker before their first application). Each usage defines: an icon, a one-line
explanation of what this space is for, and — where relevant — a direct action (e.g.,
"Log your first problem" button on an empty DSA Tracker), not just a passive "No data
yet."

## 5. Loading States: Skeletons, Not Spinners

**Decision: skeleton loaders that mirror the actual content shape, used as the
default loading pattern — generic spinners are the exception, not the rule.** A
`DataTable` skeleton renders placeholder rows in the table's actual shape; a
`StatCard` skeleton renders a placeholder number and label in place. This is both a
real perceived-performance improvement (the layout doesn't jump when data arrives)
and a visible signal of design maturity a spinner-everywhere dashboard doesn't send.
Spinners are reserved for genuinely indeterminate, layout-independent actions (e.g., a
button's own loading state, §2.1).

## 6. Documentation Convention

**Decision: no separate Storybook instance for v1.** Per your own "never
over-engineer" rule — a solo developer maintaining a full Storybook alongside the app
itself is real ongoing overhead (keeping stories in sync with component changes) for
a benefit (isolated component browsing) that mostly matters for teams coordinating
around a shared library. Instead: every primitive and composed component gets a
JSDoc block documenting its props and intended usage, colocated with the component
file — enough for future-you to understand a component without a second tool to
maintain. Revisit Storybook only if this project ever grows a real multi-person
frontend team, which isn't the v1 reality.

---

## Documentation Phase Complete

This closes the planning/architecture sequence from `02_Product_Requirements_Document.md`
through `11_Component_Library.md`. Remaining docs in the original structure
(`12_Security.md` through `20_Professional_README.md`) cover security threat modeling,
testing strategy, DevOps/deployment, Git workflow, coding standards, the Claude
working agreement, the development roadmap, and the README — each is scoped to be
written the same way: one document, fully resolving anything it inherits as an open
item from earlier docs, not restating what's already decided elsewhere.

Suggest continuing with `12_Security.md` next, since it's the one remaining document
several earlier decisions explicitly deferred to (audit logging detail, JWT/OAuth flow
diagrams, RBAC enforcement detail, file-upload validation from the architectural
review's §3 security findings) — everything else can follow in any order after that.

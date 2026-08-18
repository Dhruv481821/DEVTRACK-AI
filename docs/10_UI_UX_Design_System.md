# DevTrack AI — UI/UX Design System

**Status:** Draft v1.0
**Depends on:** `08_Frontend_Architecture.md`
**Feeds into:** `11_Component_Library.md`

This document defines the token system once, early, so Phase 1–5 components are built
against real values from day one — even though the *visual polish pass* (3D
background, aurora animation, micro-interaction detail) is deliberately scheduled in
Phase 6 (PRD §11). The tokens exist now; the full-fidelity execution of some of them
lands later. That's an intentional split, not a contradiction: a button built in
Phase 1 should already use the real color/spacing/motion tokens, so Phase 6 is a
polish pass, not a re-skin.

**Design stance, stated up front:** the brief explicitly wants something in the
Apple/Linear/Vercel/Stripe/Raycast/Cursor/Framer register, without copying any of
them. The generic AI-default answer to "premium dark SaaS" is a near-black background
with one bright accent color (acid-green or vermilion) — that's exactly the cliché to
avoid, and it's called out here on purpose so it's a documented choice, not an
accident. DevTrack's actual subject — a system that *connects* previously-scattered
developer tools into one graph — is the thing the palette and signature element below
are actually derived from, not a generic "dark mode SaaS" mood board.

---

## 1. Color Tokens

Named, not "primary-500" style scales — six colors, each with a real name and job:

| Token | Hex | Role |
|---|---|---|
| `void` | `#0B0D12` | Base background — near-black with a cool blue undertone, not pure black (pure black on OLED reads harsh at this UI density; the blue undertone also sets up the aurora gradient in §6 to feel native to the palette, not bolted on) |
| `surface` | `#14171F` | Card/panel background |
| `surface-raised` | `#1C202B` | Elevated surface (modals, popovers, command palette) |
| `border` | `#272B37` | Hairline borders, dividers |
| `signal` | `#6E5BFF` | Primary accent — electric indigo. Used for primary actions, active states, focus rings |
| `current` | `#2DD9C4` | Secondary accent — a cool teal, paired with `signal` to form the aurora gradient (§6). Never used alone as a solid fill for primary actions — its job is gradient/accent pairing, not a second primary color competing with `signal` |
| `text-primary` | `#F3F4F7` | Primary text — off-white, not pure white (pure white on `void` has more contrast than reading comfort needs at body-text sizes) |
| `text-muted` | `#8B90A0` | Secondary text, captions |
| `success` / `warning` / `danger` | `#3ECF8E` / `#F5B94D` / `#F2555A` | Status colors — used sparingly, never for decoration |

**Why `signal` + `current` instead of one flat accent:** the product's core thesis is
connecting previously-separate signals (GitHub activity, DSA practice, resume,
applications) into one picture. A two-color gradient accent that only fully resolves
where they meet is a small, consistent visual echo of that thesis, used at exactly one
level of restraint (§6) rather than everywhere.

## 2. Typography

| Role | Typeface | Usage |
|---|---|---|
| Display | **Cabinet Grotesk** | Page titles, hero headlines, section headers — a geometric grotesque with real character, not the default Inter-for-everything choice most dashboards reach for |
| Body | **Inter** | Body copy, form labels, UI chrome — chosen deliberately *because* it's the neutral, highly-legible standard for dense UI, not despite it. Display carries personality; body should get out of the way |
| Utility/Data | **JetBrains Mono** | Stats, code snippets, DSA problem metadata, timestamps — reinforces the developer-tool identity anywhere a number or code-like value appears |

**Type scale** (rem, 1rem = 16px base):

| Token | Size | Weight | Use |
|---|---|---|---|
| `display-xl` | 3.5rem | 600 | Landing hero only |
| `display-lg` | 2.25rem | 600 | Page titles |
| `heading` | 1.5rem | 600 | Section headers |
| `body` | 1rem | 400 | Default body copy |
| `body-sm` | 0.875rem | 400 | Secondary text, captions |
| `mono` | 0.875rem | 500 | Data/stats (JetBrains Mono) |

## 3. Spacing & Grid

- **Base unit: 4px**, all spacing tokens are multiples of it (4, 8, 12, 16, 24, 32,
  48, 64) — an 8pt-aligned system, the same discipline Apple/Linear/Stripe all use,
  because it's genuinely what keeps a dense dashboard from feeling arbitrary, not
  because it's fashionable.
- **Grid:** 12-column, max content width 1440px, 24px gutters on desktop, 16px on
  mobile.
- **Breakpoints:** `sm` 640px, `md` 768px, `lg` 1024px, `xl` 1280px, `2xl` 1536px —
  standard Tailwind defaults, kept rather than customized, since there's no product
  reason to diverge from them.

## 4. Elevation & Glassmorphism

Glassmorphism is used **deliberately and sparingly** — the brief warns against
"plain boxes," but overusing translucency everywhere is its own cliché and actively
hurts the WCAG contrast requirements (`NFR-A11Y-01`). Two elevation systems, used for
different purposes:

- **Solid elevation** (default for most cards): `surface` / `surface-raised`
  background + `border` + a soft shadow (`0 4px 24px rgba(0,0,0,0.24)`). This is what
  most of the dashboard uses — it's readable, performant, and accessible by default.
- **Glass elevation** (used specifically for floating/overlay UI — command palette,
  the AI Assistant panel, notification dropdown): `surface-raised` at 72% opacity +
  `backdrop-blur(20px)` + `border` at reduced opacity. Reserved for surfaces that
  visually float *above* content, where the blur communicates "this is a layer on
  top" — not applied to standard content cards, which stay solid for both
  performance and contrast-ratio reasons.

## 5. Motion & Micro-interactions

Per the skill guidance this system is built against: **orchestrated moments over
scattered effects.** Tokens:

| Token | Value | Use |
|---|---|---|
| `duration-fast` | 120ms | Hover states, button press |
| `duration-base` | 200ms | Panel open/close, tab switches |
| `duration-slow` | 400ms | Page transitions, modal entrance |
| `ease-out-expo` | `cubic-bezier(0.16, 1, 0.3, 1)` | Default easing — fast start, gentle settle, the same easing family Linear/Vercel use because it genuinely reads as "premium," not by coincidence |

**Principle, stated so it's enforceable in review, not just aspirational:** every
element gets at most one motion property animating at once (don't scale *and* fade
*and* translate the same element simultaneously — pick the one that serves the
moment). `prefers-reduced-motion` disables all non-essential motion — this is a
build requirement, not a nice-to-have, per `NFR-A11Y-01`.

## 6. The Aurora / Signature Element

**Signature element: a slow-moving aurora mesh gradient (`signal` → `current`),
rendered as a subtle animated background layer — but its actual visual behavior is
tied to the product's core thesis, not decoration for its own sake.** On the landing
page, the aurora resolves into a loose constellation of connected nodes (echoing
"previously scattered tools, now one system") as the hero content loads. On the
authenticated dashboard, it's dialed back to a much quieter ambient gradient behind
the header only — present, on-brand, but not competing with actual data-dense UI for
attention.

**Performance tradeoff, resolved explicitly (this is a real tension with
`NFR-PERF-01`'s Lighthouse target, not a hypothetical one):**

- **Landing page hero:** full React Three Fiber 3D scene is acceptable here — it's a
  single page, code-split from the authenticated app bundle, seen once per visitor,
  and marketing pages tolerate a slightly heavier initial payload in exchange for
  first-impression impact.
- **Authenticated dashboard:** the aurora here is a **CSS/SVG gradient animation**,
  not a persistent R3F/Three.js scene — a live WebGL scene running behind a data-dense
  dashboard the user has open for extended sessions is a real, continuous CPU/GPU and
  battery cost that directly fights `NFR-PERF-01`'s ≥90 authenticated-dashboard
  target. This is a deliberate scope boundary: **3D is a landing-page technique in
  this product, not a persistent dashboard background**, even though the original
  brief listed "3D background" as a general premium-UI requirement. Flagging this
  explicitly since it's a real interpretation call, not silent scope-cutting.

## 7. Accessibility Commitments (ties to `NFR-A11Y-01`/`02`)

- All text/background pairs meet WCAG AA contrast at minimum (`text-muted` on `void`
  checked specifically — muted text is the token most likely to drift below threshold
  if picked by eye rather than checked).
- Visible focus ring on every interactive element, using `signal` at full opacity —
  never suppressed for aesthetic reasons, a common real-world violation this system
  rules out at the token level (the focus-ring token isn't optional per-component).
- Glass-elevation surfaces (§4) get a contrast check separately from solid surfaces,
  since translucent backgrounds are exactly where contrast quietly fails first.

---

## Next Document

`11_Component_Library.md` — the actual component inventory (buttons, cards, forms,
the command palette, data tables) built from these tokens, plus shadcn/ui integration
conventions.

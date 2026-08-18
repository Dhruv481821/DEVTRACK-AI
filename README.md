# DevTrack AI

**The AI-Powered Developer Operating System.**

> Unifies the tools developers currently scatter across GitHub, LeetCode, Notion,
> Linear, and a dozen spreadsheets into one system — and uses AI to correlate signal
> *across* those tools, not just chat about them in isolation.

**Project status: early development (Phase 0 — Foundation).** This README describes
what DevTrack AI is and how it's built. It does not claim features that don't exist
yet — see [Project Status](#project-status) below for what's actually live.

---

## Why This Exists

Developers preparing for interviews or growing a career currently manage their code
(GitHub), their DSA practice (LeetCode/spreadsheets), their notes (Notion/Obsidian),
their resume (Word/Canva), and their job applications (another spreadsheet) as five
disconnected tools. None of them talk to each other, so a question like *"am I
actually ready for this job?"* has no single source of truth to answer it from.

DevTrack AI's bet: **the correlation between these signals is more valuable than any
one of them alone.** An AI Assistant that can see a user's resume claims, their actual
DSA solve history, and their GitHub activity in one place can say something none of
those tools can say individually — "your resume claims backend strength, but your
last 90 days of practice are almost entirely frontend-tagged." That's the product.

Full product reasoning, including an honest look at where this bet is unproven, lives
in [`/docs/02_Product_Requirements_Document.md`](./docs/02_Product_Requirements_Document.md).

## Engineering Documentation

This repository includes its **complete pre-implementation engineering blueprint** —
20 documents covering product requirements, system architecture, database design, API
specification, security threat modeling, AI architecture, and more — written *before*
a line of application code, and kept in sync with the code as it's built
(see [`18_Claude_Workflow.md`](./docs/18_Claude_Workflow.md) for how).

This isn't boilerplate documentation. It includes a self-critical
[architectural review](./docs/PRD_Architectural_Review.md) that surfaced real gaps —
a missing email provider, a connection-pooling risk with the chosen database, an
unresolved prompt-injection vector — and every subsequent document shows those gaps
actually getting resolved with reasoning, not just checked off. If you want to see how
this project's technical decisions were actually made, start there.

| Document | Covers |
|---|---|
| [`02_Product_Requirements_Document.md`](./docs/02_Product_Requirements_Document.md) | Vision, competitive landscape, user stories, phased roadmap |
| [`04_System_Architecture.md`](./docs/04_System_Architecture.md) | Modular monolith design, event-driven module boundaries |
| [`05_Database_Architecture.md`](./docs/05_Database_Architecture.md) | Schema, indexing, PostgreSQL-vs-alternatives reasoning |
| [`09_AI_Architecture.md`](./docs/09_AI_Architecture.md) | AI Service Layer, prompt-injection mitigation, real Gemini quota research |
| [`12_Security.md`](./docs/12_Security.md) | Auth flows, threat model, encryption-at-rest decisions |
| [`19_Development_Roadmap.md`](./docs/19_Development_Roadmap.md) | Current build status, sprint breakdown |

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, TypeScript, Tailwind CSS, TanStack Query, Zustand, shadcn/ui |
| Backend | Java 21, Spring Boot 3, Spring Security, Hibernate/JPA, MapStruct |
| Database | PostgreSQL (Neon), Redis (from Phase 2) |
| AI | Google Gemini (Flash) |
| Storage / Email | Cloudinary, Resend |
| Deployment | Vercel (frontend), Railway (backend), GitHub Actions (CI/CD) |

Full reasoning behind each choice — including where the original stack assumptions
were revised (e.g., Gemini Pro's free tier disappearing, requiring a switch to Flash)
— is in [`09_AI_Architecture.md`](./docs/09_AI_Architecture.md) §1 and
[`04_System_Architecture.md`](./docs/04_System_Architecture.md).

## Architecture Highlights

A few decisions worth a closer look if you're evaluating this repo's engineering
depth rather than just its feature list:

- **Modular monolith with enforced module boundaries** — cross-module coupling (e.g.,
  Calendar needing data from Job Tracker) goes through an in-process domain event
  bus, not direct cross-module calls. See `04_System_Architecture.md` §3.5.
- **AI agents are architecturally constrained to read-only** — no AI feature in this
  system can trigger a write or destructive action, which bounds the blast radius of
  prompt injection by design rather than by best-effort filtering alone. See
  `09_AI_Architecture.md` §4.
- **A 404-not-403 ownership convention, enforced by one shared helper** — every
  resource that exists but isn't owned by the requesting user returns 404, closing an
  enumeration leak, applied uniformly rather than left to per-endpoint judgment. See
  `06_API_Specification.md` §1.6.

## Getting Started (Local Development)

```bash
# Clone and start local infrastructure (Postgres, Redis from Phase 2)
git clone <repo-url>
cd devtrack-ai
docker compose up -d

# Backend
cd backend
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Full environment variable requirements are documented in
[`15_Deployment.md`](./docs/15_Deployment.md) §2. Copy `.env.example` to `.env` and
fill in the values it references — never commit a real `.env` file.

## Testing

```bash
# Backend
cd backend && ./mvnw test

# Frontend
cd frontend && npm run test
```

Test strategy, including exactly which flows are considered critical-path and
required (not just "well-tested where convenient"), is documented in
[`13_Testing.md`](./docs/13_Testing.md) §4.

## Project Status

Built solo, part-time, over a planned 12 months, in six phases — see
[`19_Development_Roadmap.md`](./docs/19_Development_Roadmap.md) for the live-updated
status table and current sprint. As of this writing: **Phase 0 (Foundation) is in
progress; no phase is complete yet.** This section is updated as phases actually
ship — not aspirationally in advance of the work.

## License

MIT — see [`LICENSE`](./LICENSE). *(Flagged as a recommendation, not a final
decision made on your behalf: MIT is the common default for a portfolio-facing repo
where the goal is showcasing the code, but confirm this is the license you actually
want before publishing.)*

## Contact

Solo project — not currently open to external contributions, but issues and feedback
are genuinely welcome.

---

*This README, and the full engineering blueprint it links to, were written before
implementation began — see [`18_Claude_Workflow.md`](./docs/18_Claude_Workflow.md)
for why that ordering matters to this project.*

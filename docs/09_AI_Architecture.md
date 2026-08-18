# DevTrack AI — AI Architecture

**Status:** Draft v1.0
**Depends on:** `08_Frontend_Architecture.md`, `04_System_Architecture.md` §3.4/§3.5/§7
**Feeds into:** `10_UI_UX_Design_System.md`

This document resolves the assumption the review flagged as missing entirely
(finding 1.1: "Gemini free-tier quota numbers are never stated anywhere") with real
numbers, then designs the AI Service Layer against those numbers — not against an
assumed-generous quota.

---

## 1. Model Choice & Real Quota Numbers

**This changes a decision, not just documents one:** Gemini Pro models have moved
behind billing as of April 2026 — <cite index="5-1">the free tier is now essentially trial-only for Pro, capped at 5 requests per minute and roughly 50 requests per day</cite>. **Decision: DevTrack AI uses Gemini Flash (or Flash-Lite for
lighter-weight agents), not Pro, for all v1 AI features** — Pro's free-tier quota is
too small to be a real product dependency, and this wasn't a live constraint when the
original tech stack was written.

Flash's free-tier numbers, which the whole rate-limiting and caching design below is
built against: <cite index="7-1">roughly 1,500 requests per day, 15 requests per minute, and up to 1 million tokens per minute</cite>, with <cite index="4-1">quotas applying at the Google Cloud project level, not per individual API key</cite>. <cite index="7-1">Free tier also has no SLA — requests can be deprioritized during peak load on Google's infrastructure</cite>.

**What this means concretely:** 1,500 requests/day is a budget shared across
**every** DevTrack user, not per-user. This single number is why aggressive caching
(§6) and a hard per-user daily cap (§7) aren't nice-to-haves — without them, a
handful of active users could exhaust the entire app's AI capacity for everyone else
well before the day resets. Token volume (1M TPM) is not the binding constraint here;
request *count* is — worth stating plainly since it changes what "optimize for" means:
optimizing prompt length barely helps; optimizing request *count* (via caching) is
what actually protects the app from running dry.

## 2. AI Service Layer

Implements the `04_System_Architecture.md` §7 decision: one backend service, six
prompt/context configurations, not six separate agent classes.

```java
interface AiProvider {
    AiResponse generate(AiRequest request);
}

record AgentConfig(
    AgentType type,
    String systemPromptTemplate,
    List<DataSource> requiredDataSources,   // which modules' service layer to read
    Class<?> outputSchema,                    // structured output shape
    Duration cacheTtl                          // null = invalidate-on-write only, see §6
)
```

`GeminiAiProvider implements AiProvider` is the only concrete provider for v1; the
interface exists so a future provider swap (or a fallback provider during an outage,
§8) touches one adapter, per SRS `NFR-MAINT-02`.

## 3. Agent Configurations

| Agent | Inputs (module data read) | Output | Ships |
|---|---|---|---|
| **AI Career Agent** | Resume Analysis + DSA activity summary + GitHub activity summary + target job description (if provided) | Cross-module readiness narrative + specific gaps (`FR-AI-01`'s hard gate — must join ≥2 sources) | Phase 5 |
| **AI Resume Agent** | Resume content + target job description | ATS score + keyword gaps + explainable reasoning (`FR-ATS-01`) | Phase 3 (ships before the rest — it's the one agent whose data dependency is available early) |
| **AI GitHub Agent** | GitHub contribution summary (not raw commit data — see §5) | Consistency/depth insight beyond raw stats | Phase 5 |
| **AI Study Planner Agent** | DSA weak-topic summary + current study plan | Suggested focus areas | Phase 5 |
| **AI Portfolio Reviewer** | Published portfolio content | Gap analysis vs. typical expectations for the user's target role | Phase 5 |
| **AI Interview Coach** | Logged interview rounds/outcomes | Pattern analysis | **Backlog** — per PRD §14 open question 4, not committed to v1; needs enough per-user interview data to be meaningful, which is unlikely in year one |

Note the Resume Agent's data dependency (Resume + job description) exists by Phase 3
— it's called out as shippable ahead of the rest specifically because there's no
reason to wait for Phase 5 just because the *other five* agents' dependencies aren't
ready yet. The "AI Assistant ships last" principle (PRD §11) is about the **Career
Agent** specifically (it needs everything), not a blanket rule that no AI feature can
exist before Phase 5. Worth correcting here since the PRD's phrasing could be read
either way.

## 4. Prompt Injection Mitigation (implementing `04_System_Architecture.md` §3.4)

Every agent's prompt is assembled in three structurally separate parts, never string-
concatenated into one undifferentiated block:

1. **System instruction** (fixed, per `AgentConfig`, never contains user data) —
   states the agent's task and explicitly instructs that content inside the data
   block below is information to analyze, not instructions to follow.
2. **User data block** — the actual resume text, notes content, etc., wrapped in an
   unambiguous, consistent delimiter (e.g., XML-style tags) that the system
   instruction explicitly names as the untrusted-content boundary.
3. **Output schema instruction** — tells the model the exact structured shape to
   return (matching `AgentConfig.outputSchema`), so the response is parsed, not
   free-text-matched.

Combined with the **hard read-only constraint** from `04_System_Architecture.md`
§3.4 (no agent can trigger a write/action), the realistic worst case of a successful
injection attempt is a manipulated *text response shown back to the same user who
authored the injected content* — not a cross-user or destructive outcome. That's a
meaningfully bounded failure mode, worth stating explicitly rather than claiming
injection is "solved" (it isn't, for any LLM system — the goal here is bounding the
blast radius, not eliminating the vector).

## 5. Context Window Budgeting

Given §1's finding that **request count, not token volume, is the binding
constraint**, context budgeting here is less about staying under a token ceiling and
more about controlling latency and avoiding sending redundant/stale data on every
call:

- Agents receive **summarized, module-service-produced views**, never raw entity
  dumps. E.g., the GitHub Agent receives a pre-aggregated `GithubActivitySummary`
  (commit frequency by week, language breakdown, top repos) computed by the
  `github` module's service layer — not a raw list of every synced commit.
- Time-bounded by default (e.g., "last 90 days" for activity-based summaries) unless
  an agent's task specifically needs longer history (the Career Agent's readiness
  narrative may reasonably want a longer DSA trend window than 90 days — decided
  per-agent in its `AgentConfig`, not globally).
- This keeps prompts small and cheap regardless of how long a user has used the app —
  a two-year power user's prompt size doesn't grow unbounded just because their
  activity history did.

## 6. Caching (extends `05_Database_Architecture.md` §3)

- **Cache key:** `hash(agentType, userId, sourceDataVersion)`, where
  `sourceDataVersion` is derived from the `updated_at` timestamps of whatever data
  sources feed that agent (§3's "requiredDataSources"). A user re-requesting the same
  agent with unchanged underlying data gets the cached response — zero additional
  Gemini requests consumed.
- **Invalidation:** write-triggered, not time-based (per §3 of the DB doc) — a
  domain event (`04_System_Architecture.md` §5's event bus) fired on relevant
  mutations (e.g., `DsaProblemLoggedEvent`) invalidates any cached AI response whose
  `sourceDataVersion` depended on that data. This is the same event-bus mechanism
  already built for Calendar/Notifications — extended here, not duplicated.

## 7. Fair-Use Policy (concrete numbers, extending `07_Backend_Architecture.md` §6)

Given the shared 1,500 requests/day project-wide budget (§1):

**Decision: cap each user at 20 AI requests/day.** At that cap, the app supports
~75 fully-active AI users per day before hitting the shared ceiling — a reasonable
target for a v1 with no paid acquisition (matches the PRD §5 SOM estimate of
low-thousands total users, of which only a fraction will be daily-active AI users on
any given day). This number is a starting point to tune against real usage once
Phase 5 ships, not a permanent constant — it's a config value, not a hardcoded
literal, specifically so it can be adjusted without a redeploy.

Enforced via the same Bucket4j Redis-backed limiter introduced in
`07_Backend_Architecture.md` §6, with its own bucket key
(`ai-quota:{userId}:{date}`) separate from the auth rate-limit buckets.

## 8. Degraded-Mode UX (resolves review finding 4.2)

Per the PRD §10 principle ("the product must not depend on the AI layer to be
useful"), the actual designed states when Gemini is unavailable or a user's quota is
exhausted:

| Situation | UI behavior |
|---|---|
| Gemini returns 429 (rate-limited) or is down | Retry with exponential backoff + jitter (standard practice for 429 handling — avoids making the problem worse with immediate retries) for a bounded number of attempts; on final failure, show the **last cached response** if one exists, labeled with its generation date, plus a clear "AI insights temporarily unavailable, showing your last result" message. |
| No cached response exists yet | Plain, non-alarming empty state: "AI insights aren't available right now — the rest of your dashboard works normally," with a manual retry action. Never a raw error or a blocked page. |
| User has exhausted their daily quota (§7) | Explicit message stating the daily limit and when it resets — not a generic error, since this is an expected, designed state, not a failure. |

## 9. Sourcing Transparency (in place of a numeric "confidence score")

Gemini doesn't provide a calibrated confidence value, and fabricating one (e.g.,
asking the model to self-report "confidence: 87%") would be presenting a number with
no real statistical grounding — worse than no number at all for a feature giving
career advice. **Decision: every AI response displays which data sources informed it**
(e.g., "Based on: your last 90 days of DSA activity, your GitHub contributions, and
your active resume") — transparency about *inputs*, not a fabricated confidence
metric about *output quality*. This also gives the user a direct way to notice when
an insight seems off ("that doesn't match my GitHub" is actionable; "confidence: 87%"
is not).

---

## Next Document

`10_UI_UX_Design_System.md` — typography, spacing, motion, color tokens, and the
premium visual language (deliberately scheduled as a Phase 6 build per PRD §11, but
the *system* itself should be designed once, early, so later phases build against it
consistently rather than improvising per-component).

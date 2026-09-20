package com.devtrack.ai;

import java.time.Duration;
import java.util.Map;

/**
 * Per-agent configuration. /docs/09_AI_Architecture.md's literal AgentConfig record also includes
 * requiredDataSources (a generic list of modules to read) and a Class<?> outputSchema (for
 * generating a JSON schema from a Java type) — both omitted here deliberately. With one agent whose
 * data-fetch is a fixed, hardcoded call into two Resume-module methods (not a generic "read from N
 * modules" traversal), requiredDataSources has no consumer yet. Generating a JSON Schema from a
 * Java class is real machinery with no second use case to justify it when hand-writing one Map is
 * just as clear. Both return when the Career Agent — which genuinely needs to enumerate
 * cross-module sources generically — gets built.
 *
 * @param cacheTtl how long a successful response stays valid in the exact-match cache before Redis
 *     expires it — the "invalidate on write" design in the docs is satisfied by the cache key
 *     itself changing when source data changes (see ResumeAtsService), so this TTL is cleanup, not
 *     a correctness mechanism
 */
public record AgentConfig(
    AgentType type,
    String systemPromptTemplate,
    Map<String, Object> responseSchema,
    Duration cacheTtl) {}

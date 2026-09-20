package com.devtrack.ai;

import java.util.Map;

/**
 * The three-part prompt structure from /docs/09_AI_Architecture.md §4's prompt-injection mitigation
 * is enforced by this type's shape, not left to caller discipline — there is no constructor that
 * accepts one pre-concatenated prompt string, so a caller cannot accidentally skip the
 * untrusted-data separation.
 *
 * @param systemInstruction the trusted instruction telling the model what to do and how to treat
 *     untrustedDataBlock — must explicitly tell the model to treat that block as data to analyze,
 *     never as instructions to follow
 * @param untrustedDataBlock user-supplied and/or user-owned data (resume content, a pasted job
 *     description, etc.) — wrapped in delimiters by the caller before being placed here
 * @param responseJsonSchema JSON Schema (Gemini's structured-output format) the model's response
 *     must conform to
 */
public record AiRequest(
    String systemInstruction, String untrustedDataBlock, Map<String, Object> responseJsonSchema) {}

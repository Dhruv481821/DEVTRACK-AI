package com.devtrack.ai;

/**
 * Provider-agnostic contract for calling a generative AI model — see /docs/09_AI_Architecture.md
 * §3. Gemini is the only implementation today (GeminiAiProvider), but no caller depends on anything
 * Gemini-specific through this interface, so swapping providers later touches one class, not every
 * agent.
 */
public interface AiProvider {

  AiResponse generate(AiRequest request);
}

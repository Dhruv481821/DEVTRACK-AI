// Turns a 429's Retry-After header (seconds) into "Xh Ym" / "Ym". Shared by every
// AI feature, since they all draw from the same daily quota (09_AI_Architecture.md §7).
export function formatRetryAfter(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.round((seconds % 3600) / 60);
  return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m`;
}

// The user-facing quota message for an AI 429, with the reset time when the header is present.
export function quotaMessage(message: string, retryAfterHeader: string | null): string {
  const seconds = retryAfterHeader ? Number(retryAfterHeader) : NaN;
  return Number.isFinite(seconds) ? `${message} Resets in ${formatRetryAfter(seconds)}.` : message;
}

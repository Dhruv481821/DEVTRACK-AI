// Shown while a route's code chunk downloads. Intentionally quiet: a short text
// status for screen readers, no spinner animation.
export function PageFallback() {
  return (
    <p className="font-body text-sm text-text-muted" role="status" aria-live="polite">
      Loading…
    </p>
  );
}

// Phase 0 exit criterion (PRD §11): "empty but fully-styled dashboard." Real
// widgets arrive as their owning modules ship (Phase 1+) — per the PRD's own
// product principle, empty states are a designed surface, not a placeholder, so
// this states plainly what's coming rather than showing a blank page.
export function DashboardPage() {
  return (
    <div>
      <h1 className="mb-2 font-display text-2xl">Dashboard</h1>
      <p className="font-body text-text-muted">
        Your modules will appear here as they ship — starting with Notes, Calendar, and
        Study Planner in Phase 1.
      </p>
    </div>
  );
}

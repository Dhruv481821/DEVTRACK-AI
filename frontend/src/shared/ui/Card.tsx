import { type HTMLAttributes } from 'react';
import clsx from 'clsx';

// Solid elevation — the default content container per
// 10_UI_UX_Design_System.md §4. Glass elevation (GlassPanel) is reserved for
// floating/overlay UI (command palette, notifications dropdown) — not built yet,
// not needed for this slice.
export function Card({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={clsx(
        'rounded-xl border border-border bg-surface p-6 shadow-[0_4px_24px_rgba(0,0,0,0.24)]',
        className,
      )}
      {...props}
    />
  );
}

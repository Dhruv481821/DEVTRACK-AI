import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import clsx from 'clsx';
import { useLogout } from '@/features/auth/api/useAuthMutations';

const NAV_ITEMS = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/profile', label: 'Profile' },
  { to: '/settings', label: 'Settings' },
];

// The persistent dashboard shell — FR-DASH-01, Phase 0 scope per
// 04_System_Architecture.md. Command palette (cmdk) and notification dropdown are
// GlassPanel-elevation UI per 10_UI_UX_Design_System.md §4/§6 — not built in this
// slice; this is the sidebar + content structure they'll eventually sit inside.
export function AppShell() {
  const logoutMutation = useLogout();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutMutation.mutate(undefined, { onSettled: () => navigate('/login', { replace: true }) });
  };

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 flex-col border-r border-border bg-surface px-4 py-6">
        <p className="mb-8 px-2 font-display text-lg">DevTrack AI</p>
        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                clsx(
                  'rounded-lg px-3 py-2 font-body text-sm transition-colors',
                  isActive
                    ? 'bg-surface-raised text-text-primary'
                    : 'text-text-muted hover:bg-surface-raised hover:text-text-primary',
                )
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button
          onClick={handleLogout}
          className="mt-auto rounded-lg px-3 py-2 text-left font-body text-sm text-text-muted hover:bg-surface-raised hover:text-text-primary"
        >
          Log out
        </button>
      </aside>
      <main className="flex-1 px-8 py-8">
        <Outlet />
      </main>
    </div>
  );
}

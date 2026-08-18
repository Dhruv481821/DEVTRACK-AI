import { type ButtonHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

// Minimal primitive — full shadcn/ui variant system is 11_Component_Library.md's
// scope, not built yet. This covers what the auth forms actually need: a primary
// action button with the real design tokens (10_UI_UX_Design_System.md §1/§5) and
// a visible loading state (§2.1 of that doc — never just disabled with no explanation).
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, loading, disabled, children, ...props }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={clsx(
        'inline-flex items-center justify-center rounded-lg bg-signal px-4 py-2.5',
        'font-body text-sm font-medium text-text-primary',
        'transition-transform duration-fast ease-out',
        'hover:brightness-110 active:scale-[0.98]',
        'disabled:opacity-50 disabled:cursor-not-allowed',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-signal focus-visible:ring-offset-2 focus-visible:ring-offset-void',
        className,
      )}
      {...props}
    >
      {loading ? 'Please wait…' : children}
    </button>
  ),
);
Button.displayName = 'Button';

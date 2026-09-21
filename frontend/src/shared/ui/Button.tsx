import { type ButtonHTMLAttributes, forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  loading?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, loading, disabled, children, ...props }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={twMerge(
        clsx(
          'inline-flex items-center justify-center rounded-lg bg-signal px-4 py-2.5',
          'font-body text-sm font-medium text-text-primary',
          'transition-transform duration-fast ease-out',
          'hover:brightness-110 active:scale-[0.98]',
          'disabled:opacity-50 disabled:cursor-not-allowed',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-signal focus-visible:ring-offset-2 focus-visible:ring-offset-void',
        ),
        className,
      )}
      {...props}
    >
      {loading ? 'Please wait…' : children}
    </button>
  ),
);
Button.displayName = 'Button';
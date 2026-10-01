import { type SelectHTMLAttributes, forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';

export const Select = forwardRef<HTMLSelectElement, SelectHTMLAttributes<HTMLSelectElement>>(
  ({ className, children, ...props }, ref) => (
    <select
      ref={ref}
      className={twMerge(
        clsx(
          'w-full rounded-lg border border-border bg-surface px-3 py-2.5',
          'font-body text-sm text-text-primary',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-signal',
        ),
        className,
      )}
      {...props}
    >
      {children}
    </select>
  ),
);
Select.displayName = 'Select';
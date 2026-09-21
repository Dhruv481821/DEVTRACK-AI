import { type TextareaHTMLAttributes, forwardRef } from 'react';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      className={twMerge(
        clsx(
          'w-full rounded-lg border border-border bg-surface px-3 py-2.5',
          'font-body text-sm text-text-primary placeholder:text-text-muted',
          'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-signal',
        ),
        className,
      )}
      {...props}
    />
  ),
);
Textarea.displayName = 'Textarea';
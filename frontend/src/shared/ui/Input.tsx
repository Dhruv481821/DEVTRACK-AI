import { type InputHTMLAttributes, type LabelHTMLAttributes, forwardRef } from 'react';
import clsx from 'clsx';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={clsx(
        'w-full rounded-lg border border-border bg-surface px-3 py-2.5',
        'font-body text-sm text-text-primary placeholder:text-text-muted',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-signal',
        className,
      )}
      {...props}
    />
  ),
);
Input.displayName = 'Input';

export function Label({ className, ...props }: LabelHTMLAttributes<HTMLLabelElement>) {
  return (
    // This generic Label primitive forwards htmlFor through props.
    // The jsx-a11y rule cannot trace the forwarded prop through the
    // component boundary, so it reports a false positive here.
    // All concrete usages provide htmlFor to associate the label with a control.
    // eslint-disable-next-line jsx-a11y/label-has-associated-control
    <label
      className={clsx('mb-1.5 block font-body text-sm font-medium text-text-primary', className)}
      {...props}
    />
  );
}

export function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="mt-1.5 font-body text-sm text-danger">{message}</p>;
}

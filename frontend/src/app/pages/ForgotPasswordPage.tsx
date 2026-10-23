import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { ApiError } from '@/shared/api-client/types';
import { AuthCard } from '@/features/auth/components/AuthCard';
import { useRequestPasswordReset } from '@/features/auth/api/useAuthMutations';
import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from '@/features/auth/schemas/authSchemas';

export function ForgotPasswordPage() {
  const requestReset = useRequestPasswordReset();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({ resolver: zodResolver(forgotPasswordSchema) });

  const rateLimited = requestReset.error instanceof ApiError && requestReset.error.status === 429;

  if (requestReset.isSuccess) {
    return (
      <AuthCard title="Check your email">
        {/* Same message whether or not the account exists (no account enumeration). */}
        <p className="font-body text-sm text-text-muted">
          If an account exists for that email, we&apos;ve sent a link to reset your password. It
          expires in 30 minutes.
        </p>
        <Link to="/login" className="font-body text-sm text-signal hover:underline">
          Back to log in
        </Link>
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Reset your password">
      <p className="font-body text-sm text-text-muted">
        Enter your account email and we&apos;ll send you a reset link.
      </p>
      <form
        onSubmit={handleSubmit((v) => requestReset.mutate(v.email))}
        className="space-y-4"
        noValidate
      >
        <div>
          <Label htmlFor="forgot-email">Email</Label>
          <Input id="forgot-email" type="email" autoComplete="email" {...register('email')} />
          <FieldError message={errors.email?.message} />
        </div>
        {requestReset.isError && (
          <FieldError
            message={
              rateLimited
                ? 'Too many attempts. Please try again in a minute.'
                : "Couldn't send the email. Please try again."
            }
          />
        )}
        <Button type="submit" loading={requestReset.isPending} className="w-full">
          Send reset link
        </Button>
      </form>
      <Link to="/login" className="block font-body text-sm text-signal hover:underline">
        Back to log in
      </Link>
    </AuthCard>
  );
}

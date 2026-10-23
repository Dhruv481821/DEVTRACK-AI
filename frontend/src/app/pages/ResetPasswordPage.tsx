import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useSearchParams } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { AuthCard } from '@/features/auth/components/AuthCard';
import { useConfirmPasswordReset } from '@/features/auth/api/useAuthMutations';
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '@/features/auth/schemas/authSchemas';

// Target of the link in the password-reset email: /reset-password?token=...
export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';
  const confirmReset = useConfirmPasswordReset();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({ resolver: zodResolver(resetPasswordSchema) });

  if (!token) {
    return (
      <AuthCard title="Invalid reset link">
        <p className="font-body text-sm text-text-muted">
          This link is incomplete. Request a new one.
        </p>
        <Link to="/forgot-password" className="font-body text-sm text-signal hover:underline">
          Request a new reset link
        </Link>
      </AuthCard>
    );
  }

  if (confirmReset.isSuccess) {
    return (
      <AuthCard title="Password updated">
        <p className="font-body text-sm text-text-muted">
          Your password has been changed. Log in with your new password.
        </p>
        <Link to="/login" className="font-body text-sm text-signal hover:underline">
          Go to log in
        </Link>
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Choose a new password">
      <form
        onSubmit={handleSubmit((v) => confirmReset.mutate({ token, newPassword: v.newPassword }))}
        className="space-y-4"
        noValidate
      >
        <div>
          <Label htmlFor="new-password">New password</Label>
          <Input
            id="new-password"
            type="password"
            autoComplete="new-password"
            {...register('newPassword')}
          />
          <FieldError message={errors.newPassword?.message} />
        </div>
        <div>
          <Label htmlFor="confirm-password">Confirm password</Label>
          <Input
            id="confirm-password"
            type="password"
            autoComplete="new-password"
            {...register('confirmPassword')}
          />
          <FieldError message={errors.confirmPassword?.message} />
        </div>
        {confirmReset.isError && (
          <div className="space-y-1">
            <FieldError
              message={
                confirmReset.error instanceof Error
                  ? confirmReset.error.message
                  : "Couldn't reset your password."
              }
            />
            <Link to="/forgot-password" className="font-body text-sm text-signal hover:underline">
              Request a new reset link
            </Link>
          </div>
        )}
        <Button type="submit" loading={confirmReset.isPending} className="w-full">
          Update password
        </Button>
      </form>
    </AuthCard>
  );
}

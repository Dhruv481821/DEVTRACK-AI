import { useEffect, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { AuthCard } from '@/features/auth/components/AuthCard';
import { useVerifyEmail } from '@/features/auth/api/useAuthMutations';

// Target of the link in the verification email: /verify-email?token=...
export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') ?? '';
  const verify = useVerifyEmail();
  const { mutate } = verify;
  // Tokens are single-use: guard against React StrictMode running the effect twice in dev,
  // which would make the second call fail and show an error after a successful verification.
  const sent = useRef(false);

  useEffect(() => {
    if (token && !sent.current) {
      sent.current = true;
      mutate(token);
    }
  }, [token, mutate]);

  if (!token || verify.isError) {
    return (
      <AuthCard title="Verification link invalid">
        <p className="font-body text-sm text-text-muted">
          {verify.error instanceof Error
            ? verify.error.message
            : 'This link is incomplete or has already been used.'}
        </p>
        <Link to="/login" className="font-body text-sm text-signal hover:underline">
          Go to log in
        </Link>
      </AuthCard>
    );
  }

  if (verify.isSuccess) {
    return (
      <AuthCard title="Email verified">
        <p className="font-body text-sm text-text-muted">Thanks — your email is confirmed.</p>
        <Link to="/dashboard" className="font-body text-sm text-signal hover:underline">
          Continue to DevTrack AI
        </Link>
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Verifying your email…">
      <p className="font-body text-sm text-text-muted" role="status" aria-live="polite">
        One moment.
      </p>
    </AuthCard>
  );
}

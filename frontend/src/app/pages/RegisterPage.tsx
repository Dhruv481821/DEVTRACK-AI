import { Link } from 'react-router-dom';
import { LegalFooter } from '@/features/legal/components/LegalFooter';
import { RegisterForm } from '@/features/auth/components/RegisterForm';

export function RegisterPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-4">
      <h1 className="font-display text-2xl">Create your DevTrack AI account</h1>
      <RegisterForm />
      <p className="max-w-sm text-center font-body text-xs text-text-muted">
        By creating an account you agree to the{' '}
        <Link to="/terms" className="text-signal hover:underline">
          Terms
        </Link>{' '}
        and{' '}
        <Link to="/privacy" className="text-signal hover:underline">
          Privacy Policy
        </Link>
        .
      </p>
      <p className="font-body text-sm text-text-muted">
        Already have an account? <Link to="/login" className="text-signal hover:underline">Log in</Link>
      </p>
      <LegalFooter />
    </div>
  );
}

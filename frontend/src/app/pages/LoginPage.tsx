import { Link } from 'react-router-dom';
import { LoginForm } from '@/features/auth/components/LoginForm';

export function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-4">
      <h1 className="font-display text-2xl">Log in to DevTrack AI</h1>
      <LoginForm />
      <p className="font-body text-sm text-text-muted">
        No account? <Link to="/register" className="text-signal hover:underline">Sign up</Link>
      </p>
    </div>
  );
}

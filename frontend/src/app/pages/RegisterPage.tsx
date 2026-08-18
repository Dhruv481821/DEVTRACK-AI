import { Link } from 'react-router-dom';
import { RegisterForm } from '@/features/auth/components/RegisterForm';

export function RegisterPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-4">
      <h1 className="font-display text-2xl">Create your DevTrack AI account</h1>
      <RegisterForm />
      <p className="font-body text-sm text-text-muted">
        Already have an account? <Link to="/login" className="text-signal hover:underline">Log in</Link>
      </p>
    </div>
  );
}

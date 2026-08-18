import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useLogin } from '../api/useAuthMutations';
import { loginSchema, type LoginFormValues } from '../schemas/authSchemas';

// Same fallback reasoning as apiClient.ts — see that file's comment.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const GOOGLE_OAUTH_URL = `${API_BASE_URL}/oauth2/authorization/google`;

export function LoginForm() {
  const navigate = useNavigate();
  const loginMutation = useLogin();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  const onSubmit = (values: LoginFormValues) => {
    loginMutation.mutate(values, {
      onSuccess: () => navigate('/dashboard'),
      // Deliberately generic here too — matches the backend's own generic
      // AUTH_INVALID_CREDENTIALS message (12_Security.md §2.2's enumeration-safety
      // rule), not overridden with something more "helpful" that leaks account
      // existence.
      onError: (err) => setError('root', { message: err instanceof Error ? err.message : 'Login failed.' }),
    });
  };

  return (
    <div className="w-full max-w-sm space-y-4">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" autoComplete="email" {...register('email')} />
          <FieldError message={errors.email?.message} />
        </div>
        <div>
          <Label htmlFor="password">Password</Label>
          <Input id="password" type="password" autoComplete="current-password" {...register('password')} />
          <FieldError message={errors.password?.message} />
        </div>
        <FieldError message={errors.root?.message} />
        <Button type="submit" loading={loginMutation.isPending} className="w-full">
          Log in
        </Button>
      </form>

      <div className="flex items-center gap-3 text-text-muted">
        <div className="h-px flex-1 bg-border" />
        <span className="font-body text-xs">or</span>
        <div className="h-px flex-1 bg-border" />
      </div>

      {/* FR-AUTH-02's Google path — a plain link, since this is a full browser
          redirect flow (SecurityConfig's oauth2Login), not a JS-driven popup. */}
      <a
        href={GOOGLE_OAUTH_URL}
        className="flex w-full items-center justify-center rounded-lg border border-border bg-surface px-4 py-2.5 font-body text-sm text-text-primary hover:bg-surface-raised"
      >
        Continue with Google
      </a>
    </div>
  );
}

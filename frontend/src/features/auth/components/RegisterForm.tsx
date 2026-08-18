import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { ApiError } from '@/shared/api-client/types';
import { useRegister } from '../api/useAuthMutations';
import { registerSchema, type RegisterFormValues } from '../schemas/authSchemas';

export function RegisterForm() {
  const navigate = useNavigate();
  const registerMutation = useRegister();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) });

  const onSubmit = (values: RegisterFormValues) => {
    registerMutation.mutate(values, {
      onSuccess: () => navigate('/login?registered=true'),
      onError: (err) => {
        // Maps backend VALIDATION_ERROR field details onto the same RHF field-error
        // state client-caught errors use — one display path for both, per
        // 08_Frontend_Architecture.md §6 (e.g. "email already taken" only the
        // server can catch).
        if (err instanceof ApiError && err.details) {
          err.details.forEach((d) => setError(d.field as keyof RegisterFormValues, { message: d.reason }));
        } else {
          setError('root', { message: err instanceof Error ? err.message : 'Registration failed.' });
        }
      },
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-sm space-y-4">
      <div>
        <Label htmlFor="email">Email</Label>
        <Input id="email" type="email" autoComplete="email" {...register('email')} />
        <FieldError message={errors.email?.message} />
      </div>
      <div>
        <Label htmlFor="password">Password</Label>
        <Input id="password" type="password" autoComplete="new-password" {...register('password')} />
        <FieldError message={errors.password?.message} />
      </div>
      <FieldError message={errors.root?.message} />
      <Button type="submit" loading={registerMutation.isPending} className="w-full">
        Create account
      </Button>
    </form>
  );
}

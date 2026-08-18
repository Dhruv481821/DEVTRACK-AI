import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useProfile, useUpdateProfile } from '../api/useProfile';
import { profileSchema, type ProfileFormValues } from '../schemas/profileSchemas';

export function ProfileForm() {
  const { data: profile, isLoading } = useProfile();
  const updateMutation = useUpdateProfile();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<ProfileFormValues>({ resolver: zodResolver(profileSchema) });

  // Populate the form once the real data arrives — can't default values before
  // the query resolves, and resetting on every render would fight the user's
  // in-progress edits.
  useEffect(() => {
    if (profile) {
      reset({ displayName: profile.displayName ?? '', bio: profile.bio ?? '' });
    }
  }, [profile, reset]);

  if (isLoading) {
    // Skeleton loader is 10_UI_UX_Design_System.md §5 / 11_Component_Library.md §5's
    // documented default — a real content-shaped skeleton is a follow-up polish
    // item, not built in this slice; a plain loading line is the honest interim.
    return <p className="font-body text-sm text-text-muted">Loading profile…</p>;
  }

  return (
    <form onSubmit={handleSubmit((values) => updateMutation.mutate(values))} className="max-w-md space-y-4">
      <div>
        <Label htmlFor="displayName">Display name</Label>
        <Input id="displayName" {...register('displayName')} />
        <FieldError message={errors.displayName?.message} />
      </div>
      <div>
        <Label htmlFor="bio">Bio</Label>
        <Input id="bio" {...register('bio')} />
        <FieldError message={errors.bio?.message} />
      </div>
      <Button type="submit" loading={updateMutation.isPending} disabled={!isDirty}>
        Save changes
      </Button>
      {updateMutation.isSuccess && <p className="font-body text-sm text-success">Saved.</p>}
    </form>
  );
}

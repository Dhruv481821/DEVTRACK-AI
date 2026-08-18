import { z } from 'zod';

// Mirrors backend UpdateProfileRequest exactly (08_Frontend_Architecture.md §6).
export const profileSchema = z.object({
  displayName: z.string().max(100, 'Display name must be 100 characters or fewer').optional(),
  bio: z.string().max(500, 'Bio must be 500 characters or fewer').optional(),
});
export type ProfileFormValues = z.infer<typeof profileSchema>;

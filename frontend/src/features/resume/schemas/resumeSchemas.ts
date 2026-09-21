import { z } from 'zod';

// Mirrors backend CreateResumeRequest/UpdateResumeRequest exactly
// (08_Frontend_Architecture.md §6).
export const createResumeSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200, 'Title must be 200 characters or fewer'),
});
export type CreateResumeFormValues = z.infer<typeof createResumeSchema>;

export const updateResumeSchema = z.object({
  title: z.string().max(200, 'Title must be 200 characters or fewer').optional(),
});
export type UpdateResumeFormValues = z.infer<typeof updateResumeSchema>;
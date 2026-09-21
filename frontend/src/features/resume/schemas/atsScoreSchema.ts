import { z } from 'zod';

// Mirrors backend AtsScoreRequest exactly (09_AI_Architecture.md's design review).
export const atsScoreSchema = z.object({
  jobDescription: z
    .string()
    .min(1, 'Job description is required')
    .max(20000, 'Job description must be 20,000 characters or fewer'),
});
export type AtsScoreFormValues = z.infer<typeof atsScoreSchema>;
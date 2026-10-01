import { z } from 'zod';

export const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD'] as const;
export type Difficulty = (typeof DIFFICULTIES)[number];

export const DIFFICULTY_LABELS: Record<Difficulty, string> = {
  EASY: 'Easy',
  MEDIUM: 'Medium',
  HARD: 'Hard',
};

// Mirrors backend CreateDsaProblemRequest exactly. Tags are edited as one
// comma-separated line rather than a dynamic add/remove-tag field array, to keep the
// form simple — matches the same trade-off made for Resume Builder's bulleted lists.
export const createDsaProblemSchema = z.object({
  title: z.string().min(1, 'Title is required').max(200, 'Title must be 200 characters or fewer'),
  difficulty: z.enum(DIFFICULTIES),
  tagsText: z.string().optional(),
});
export type CreateDsaProblemFormValues = z.infer<typeof createDsaProblemSchema>;

export function tagsTextToArray(tagsText: string | undefined): string[] {
  return (tagsText ?? '')
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean);
}
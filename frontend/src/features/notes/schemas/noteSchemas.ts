import { z } from 'zod';

export const noteFormSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(200, 'Title must be 200 characters or fewer'),
  body: z.string(),
  // Comma-separated in the form; split into tagNames on submit.
  tags: z.string(),
});

export type NoteFormValues = z.infer<typeof noteFormSchema>;

export function parseTags(raw: string): string[] {
  const seen = new Set<string>();
  for (const part of raw.split(',')) {
    const tag = part.trim().toLowerCase();
    if (tag) seen.add(tag);
  }
  return [...seen];
}

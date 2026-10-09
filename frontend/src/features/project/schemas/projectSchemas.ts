import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

export const createProjectSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .max(200, 'Name must be 200 characters or fewer'),

  description: z.string().optional().transform(blankToUndefined),

  // Optional link to a synced GitHub repo (a repo snapshot id from /api/v1/github/repos).
  linkedRepoSnapshotId: z.string().optional().transform(blankToUndefined),
});

export type CreateProjectFormValues = z.infer<typeof createProjectSchema>;

export const createTaskSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(200, 'Title must be 200 characters or fewer'),

  notes: z.string().optional().transform(blankToUndefined),
});

export type CreateTaskFormValues = z.infer<typeof createTaskSchema>;
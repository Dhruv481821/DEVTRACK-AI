import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

export const createStudyPlanSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(200, 'Title must be 200 characters or fewer'),
  // <input type="date"> -> backend LocalDate ("YYYY-MM-DD"), sent as-is.
  targetDate: z.string().optional().transform(blankToUndefined),
});

export type CreateStudyPlanFormValues = z.infer<typeof createStudyPlanSchema>;

export const createStudyTaskSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(200, 'Title must be 200 characters or fewer'),
  dueDate: z.string().optional().transform(blankToUndefined),
});

export type CreateStudyTaskFormValues = z.infer<typeof createStudyTaskSchema>;

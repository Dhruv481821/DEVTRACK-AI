import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

export const createEventSchema = z
  .object({
    title: z
      .string()
      .min(1, 'Title is required')
      .max(200, 'Title must be 200 characters or fewer'),
    description: z
      .string()
      .max(2000, 'Description must be 2000 characters or fewer')
      .optional()
      .transform(blankToUndefined),
    date: z.string().min(1, 'Date is required'),
    startTime: z.string().optional().transform(blankToUndefined),
    endTime: z.string().optional().transform(blankToUndefined),
  })
  .refine((v) => !v.endTime || !v.startTime || v.endTime > v.startTime, {
    message: 'End time must be after start time',
    path: ['endTime'],
  })
  .refine((v) => !v.endTime || v.startTime, {
    message: 'Set a start time before an end time',
    path: ['endTime'],
  });

export type CreateEventFormValues = z.infer<typeof createEventSchema>;

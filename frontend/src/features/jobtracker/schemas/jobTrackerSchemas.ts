import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

export const createJobApplicationSchema = z.object({
  companyName: z
    .string()
    .min(1, 'Company is required')
    .max(200, 'Company must be 200 characters or fewer'),

  roleTitle: z
    .string()
    .min(1, 'Role is required')
    .max(200, 'Role must be 200 characters or fewer'),

  jobUrl: z
    .string()
    .max(500, 'Job URL must be 500 characters or fewer')
    .optional()
    .transform(blankToUndefined),

  notes: z.string().optional().transform(blankToUndefined),
});

export type CreateJobApplicationFormValues = z.infer<
  typeof createJobApplicationSchema
>;

export const createInterviewRoundSchema = z.object({
  roundName: z
    .string()
    .min(1, 'Round name is required')
    .max(200, 'Round name must be 200 characters or fewer'),

  // <input type="date"> value (YYYY-MM-DD); converted to an Instant on submit.
  occurredAt: z.string().optional().transform(blankToUndefined),

  notes: z.string().optional().transform(blankToUndefined),
});

export type CreateInterviewRoundFormValues = z.infer<
  typeof createInterviewRoundSchema
  >;


export type CreateInterviewRoundPayload = CreateInterviewRoundFormValues;
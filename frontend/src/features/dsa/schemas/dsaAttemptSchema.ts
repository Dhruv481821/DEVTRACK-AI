import { z } from 'zod';

// Mirrors backend CreateDsaAttemptRequest exactly — timeTakenMinutes/notes are optional there
// (plain Integer/String, no @NotNull), kept optional here too. timeTakenMinutes stays a string
// in form state (native number inputs don't cleanly round-trip through react-hook-form as
// numbers when empty) and is converted at the form/API boundary — see attemptFormToRequest.
export const createDsaAttemptSchema = z.object({
  attemptedAt: z.string().min(1, 'Date is required'),
  timeTakenMinutes: z
    .string()
    .optional()
    .refine((value) => !value || /^\d+$/.test(value), 'Enter a whole number of minutes'),
  notes: z.string().max(2000, 'Notes must be 2,000 characters or fewer').optional(),
});
export type CreateDsaAttemptFormValues = z.infer<typeof createDsaAttemptSchema>;

export function attemptFormToRequest(values: CreateDsaAttemptFormValues) {
  return {
    attemptedAt: values.attemptedAt,
    timeTakenMinutes: values.timeTakenMinutes ? Number(values.timeTakenMinutes) : null,
    notes: values.notes || null,
  };
}
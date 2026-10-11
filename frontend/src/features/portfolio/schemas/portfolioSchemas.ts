import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

// Mirrors the backend @Pattern on UpdatePortfolioRequest.slug.
export const portfolioSettingsSchema = z.object({
  slug: z
    .string()
    .optional()
    .transform(blankToUndefined)
    .refine(
      (val) => val === undefined || /^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$/.test(val),
      'Use 3-30 lowercase letters, numbers or hyphens.',
    ),

  headline: z
    .string()
    .max(160, 'Headline must be 160 characters or fewer')
    .optional()
    .transform(blankToUndefined),

  published: z.boolean(),
});

export type PortfolioSettingsFormValues = z.infer<
  typeof portfolioSettingsSchema
>;
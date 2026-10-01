import { z } from 'zod';

const blankToUndefined = (
  val: string | undefined,
): string | undefined => (val === '' || val === undefined ? undefined : val);

export const createCertificateSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .max(200, 'Name must be 200 characters or fewer'),

  issuingOrg: z
    .string()
    .min(1, 'Issuing organization is required')
    .max(200, 'Issuing organization must be 200 characters or fewer'),

  issueDate: z.string().optional().transform(blankToUndefined),

  verificationUrl: z
    .string()
    .max(500, 'Verification URL must be 500 characters or fewer')
    .optional()
    .transform(blankToUndefined),
});

export type CreateCertificateFormValues = z.infer<
  typeof createCertificateSchema
>;

export const updateCertificateSchema = z.object({
  name: z
    .string()
    .max(200, 'Name must be 200 characters or fewer')
    .optional()
    .transform(blankToUndefined),

  issuingOrg: z
    .string()
    .max(200, 'Issuing organization must be 200 characters or fewer')
    .optional()
    .transform(blankToUndefined),

  issueDate: z.string().optional().transform(blankToUndefined),

  verificationUrl: z
    .string()
    .max(500, 'Verification URL must be 500 characters or fewer')
    .optional()
    .transform(blankToUndefined),
});

export type UpdateCertificateFormValues = z.infer<
  typeof updateCertificateSchema
>;
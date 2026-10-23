import { z } from 'zod';

// Mirrors backend CareerAssistantRequest (@NotBlank, max 1000).
export const careerAssistantSchema = z.object({
  question: z
    .string()
    .trim()
    .min(1, 'Ask a question')
    .max(1000, 'Question must be 1,000 characters or fewer'),
});

export type CareerAssistantFormValues = z.infer<typeof careerAssistantSchema>;

import { z } from 'zod';

// Mirrors backend RegisterRequest/LoginRequest exactly (08_Frontend_Architecture.md
// §6) — this is UX, the server-side validation is the real boundary.
const passwordSchema = z
  .string()
  .min(8, 'Password must be at least 8 characters')
  .regex(/[A-Z]/, 'Password must contain an uppercase letter')
  .regex(/\d/, 'Password must contain a number');

export const registerSchema = z.object({
  email: z.string().email('Enter a valid email'),
  password: passwordSchema,
});
export type RegisterFormValues = z.infer<typeof registerSchema>;

export const loginSchema = z.object({
  email: z.string().min(1, 'Email is required'),
  password: z.string().min(1, 'Password is required'),
});
export type LoginFormValues = z.infer<typeof loginSchema>;

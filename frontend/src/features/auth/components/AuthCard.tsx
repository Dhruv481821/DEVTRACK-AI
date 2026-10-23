import type { ReactNode } from 'react';
import { LegalFooter } from '@/features/legal/components/LegalFooter';

// Shared centred shell for the auth side pages (forgot/reset password, verify email),
// matching the existing Login/Register page layout.
export function AuthCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-6 px-4">
      <h1 className="text-center font-display text-2xl">{title}</h1>
      <div className="w-full max-w-sm space-y-4">{children}</div>
      <LegalFooter />
    </div>
  );
}

// Public business details shown on the legal, pricing and contact pages.
// Razorpay's KYC review checks these pages, and the name and address here must
// match what you submit to Razorpay. Change them in this one file.
export const BUSINESS = {
  name: 'Devtrack-Ai',
  operator: 'Dhruv Sharma',
  city: 'Bhiwani',
  state: 'Haryana',
  country: 'India',
  // Set VITE_SUPPORT_EMAIL in the frontend environment (Vercel → Settings → Environment
  // Variables). Kept out of source so it can change without a code edit.
  supportEmail: import.meta.env.VITE_SUPPORT_EMAIL ?? '',
  policiesUpdated: '23 October 2026',
} as const;

// Shown on the public pricing page, which can't call the authenticated billing API.
// Must match backend BillingPlan and devtrack.ai.*-daily-request-limit.
export const PUBLIC_PRICING = {
  freeAiPerDay: 3,
  proAiPerDay: 30,
  proMonthlyInr: 199,
  proYearlyInr: 1999,
} as const;

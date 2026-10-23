// Loads Razorpay Standard Checkout on demand (only when someone clicks Buy), so
// the third-party script never runs on any other page.
const CHECKOUT_SRC = 'https://checkout.razorpay.com/v1/checkout.js';

export interface RazorpaySuccess {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
}

export interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  order_id: string;
  name: string;
  description: string;
  handler: (response: RazorpaySuccess) => void;
  modal?: { ondismiss?: () => void };
  theme?: { color?: string };
}

interface RazorpayInstance {
  open: () => void;
  on: (event: 'payment.failed', cb: (response: { error: { description?: string } }) => void) => void;
}

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayOptions) => RazorpayInstance;
  }
}

let loading: Promise<void> | null = null;

export function loadRazorpay(): Promise<void> {
  if (window.Razorpay) return Promise.resolve();
  if (!loading) {
    loading = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = CHECKOUT_SRC;
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => {
        loading = null;
        reject(new Error('Could not load Razorpay Checkout'));
      };
      document.body.appendChild(script);
    });
  }
  return loading;
}

export function openCheckout(
  options: RazorpayOptions,
  onFailed: (message: string) => void,
): void {
  if (!window.Razorpay) throw new Error('Razorpay Checkout is not loaded');
  const checkout = new window.Razorpay(options);
  checkout.on('payment.failed', (response) =>
    onFailed(response.error.description ?? 'Payment failed.'),
  );
  checkout.open();
}

export function formatInr(amountPaise: number): string {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(amountPaise / 100);
}

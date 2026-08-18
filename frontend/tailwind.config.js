/** @type {import('tailwindcss').Config} */
// Token values from /docs/10_UI_UX_Design_System.md §1–3 — this file is the single
// place those tokens become real Tailwind theme values.
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        void: '#0B0D12',
        surface: '#14171F',
        'surface-raised': '#1C202B',
        border: '#272B37',
        signal: '#6E5BFF',
        current: '#2DD9C4',
        'text-primary': '#F3F4F7',
        'text-muted': '#8B90A0',
        success: '#3ECF8E',
        warning: '#F5B94D',
        danger: '#F2555A',
      },
      fontFamily: {
        display: ['"Cabinet Grotesk"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      spacing: {
        // 4px base unit — 10_UI_UX_Design_System.md §3
      },
    },
  },
  plugins: [],
};

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

// See /docs/08_Frontend_Architecture.md — route-based code splitting handled at
// the router level (React.lazy), not here.
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  // Vite normally only reads .env from its own project directory (frontend/),
  // but 15_Deployment.md §2's checklist treats frontend and backend env vars as
  // one unified list in a single root .env — this makes Vite actually honor
  // that, instead of silently needing a second, separate frontend/.env file
  // nobody documented. This was the root cause of VITE_API_BASE_URL resolving
  // to `undefined` — the root .env existed, Vite just never looked there.
  envDir: path.resolve(__dirname, '..'),
});

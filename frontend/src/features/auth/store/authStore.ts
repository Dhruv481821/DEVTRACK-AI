import { create } from 'zustand';

// Per /docs/08_Frontend_Architecture.md §4: the access token lives here, in memory
// only — never localStorage/sessionStorage. This is the XSS-mitigation half of the
// auth design; the refresh token never reaches JS at all (httpOnly cookie).
interface AuthState {
  accessToken: string | null;
  currentUser: { id: string; email: string } | null;
  setAccessToken: (token: string) => void;
  setCurrentUser: (user: { id: string; email: string }) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  currentUser: null,
  setAccessToken: (accessToken) => set({ accessToken }),
  setCurrentUser: (currentUser) => set({ currentUser }),
  clear: () => set({ accessToken: null, currentUser: null }),
}));

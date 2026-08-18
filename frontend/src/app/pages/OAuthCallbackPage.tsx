import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '@/features/auth/store/authStore';

// The frontend half of FR-AUTH-02's Google flow — OAuth2LoginSuccessHandler
// (backend) redirects here with ?token=... after a successful Google login. The
// refresh token never appears here at all; it was already set as an httpOnly
// cookie by the same backend redirect (08_Frontend_Architecture.md §4).
export function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const setAccessToken = useAuthStore((s) => s.setAccessToken);
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    if (token) {
      setAccessToken(token);
      navigate('/dashboard', { replace: true });
    } else {
      navigate('/login', { replace: true });
    }
  }, [searchParams, setAccessToken, navigate]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="font-body text-text-muted">Signing you in…</p>
    </div>
  );
}

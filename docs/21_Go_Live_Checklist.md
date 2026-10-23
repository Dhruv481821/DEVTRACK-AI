# DevTrack AI — Go-Live Checklist

Production on free URLs first (`*.vercel.app` + `*.up.railway.app`); a custom domain can
be added later without code changes. Do the steps in order — later steps need URLs and
keys created by earlier ones.

## Topology (why it is set up this way)

The browser only ever talks to the **Vercel domain**. `frontend/vercel.json` proxies
`/api/**`, `/oauth2/**` and `/login/oauth2/**` to the Railway backend. This keeps the
refresh-token cookie (`httpOnly; Secure; SameSite=Strict`) first-party. Calling Railway
directly from the browser would make it a cross-site cookie that browsers drop, logging
users out on every reload.

If the Railway public URL is not `https://devtrack-ai-production.up.railway.app`, update
the three `destination` values in `frontend/vercel.json` before deploying.

## 1. Accounts and keys

- [ ] Support email created (e.g. a dedicated Gmail).
- [ ] Gemini API key (Google AI Studio). Check `devtrack.gemini.model` in
      `application.yml` against Google's current model list.
- [ ] Neon Postgres database → pooled connection string.
- [ ] Redis (Railway Redis plugin or Upstash) → `REDIS_URL`.
- [ ] Resend API key (verification and password-reset emails).
- [ ] JWT RS256 keypair (`JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`) and a 32-byte base64
      `TOKEN_ENCRYPTION_KEY` — generate fresh for production; never reuse the dev values.

## 2. Backend on Railway

- [ ] Service deploys from this repo's `backend/`; note the public URL.
- [ ] Environment variables:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Neon |
| `REDIS_URL` | Redis |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`, `TOKEN_ENCRYPTION_KEY` | step 1 |
| `GEMINI_API_KEY`, `RESEND_API_KEY` | step 1 |
| `CORS_ALLOWED_ORIGIN` | `https://<vercel-domain>` |
| `FRONTEND_BASE_URL` | `https://<vercel-domain>` |
| `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET` | step 4 |
| `GOOGLE_OAUTH_REDIRECT_URI` | `https://<vercel-domain>/login/oauth2/code/google` |
| `GITHUB_OAUTH_CLIENT_ID`, `GITHUB_OAUTH_CLIENT_SECRET` | step 4 |
| `GITHUB_OAUTH_REDIRECT_URI` | `https://<vercel-domain>/api/v1/github/callback` |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET` | step 5 |
| `CLOUDINARY_*` | only if avatar upload is enabled |

- [ ] `https://<railway-url>/actuator/health` returns `UP` (Flyway migrations run on startup).

## 3. Frontend on Vercel

- [ ] Project root `frontend/`, framework Vite.
- [ ] Environment variables: `VITE_API_BASE_URL` = **empty value** (same-origin via the
      proxy), `VITE_SUPPORT_EMAIL` = your support email.
- [ ] Open `https://<vercel-domain>/pricing`, `/terms`, `/privacy`, `/refunds`, `/contact`
      logged out — all must load and show the email.

## 4. OAuth apps

- [ ] Google Cloud Console → OAuth client → Authorized redirect URI:
      `https://<vercel-domain>/login/oauth2/code/google`; authorized JavaScript origin:
      `https://<vercel-domain>`.
- [ ] GitHub → Developer settings → OAuth App → callback URL:
      `https://<vercel-domain>/api/v1/github/callback`.

## 5. Razorpay

- [ ] Website for KYC: `https://<vercel-domain>` (pricing and the four policy pages are public).
- [ ] Complete KYC; while waiting, test-mode keys work end to end.
- [ ] After activation: live API keys → `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET`.
- [ ] Webhook → URL `https://<railway-url>/api/v1/billing/webhook` (server-to-server, the
      Railway URL is fine), events `payment.captured` and `order.paid`, secret →
      `RAZORPAY_WEBHOOK_SECRET`.

## 6. CI deploys

- [ ] GitHub → Settings → Secrets: `RAILWAY_TOKEN`
      (a Railway project token).
- [ ] GitHub → Settings → Variables: `RAILWAY_DEPLOY_ENABLED` = `true`.
- [ ] Turn off Railway's own auto-deploy so CI (tests first) is the only deploy path.

## 7. Smoke test in production

- [ ] Register with email → verification email arrives → log in → reload the page: still
      logged in (proves the first-party cookie works).
- [ ] Sign in with Google; connect GitHub; repos appear on the dashboard.
- [ ] Ask the AI Assistant one question.
- [ ] Buy Pro with a real ₹199 payment, confirm Pro activates, then refund it from the
      Razorpay dashboard.
- [ ] Publish a portfolio and open `/u/<slug>` in a private window.

## Later: custom domain

Add the domain in Vercel, then update every `<vercel-domain>` value above (Railway env
vars, Google/GitHub OAuth apps, Razorpay website). No code changes are needed.

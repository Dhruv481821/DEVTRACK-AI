import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OAuthCallbackPage } from './pages/OAuthCallbackPage';
import { RequireAuth } from './layouts/RequireAuth';
import { AppShell } from './layouts/AppShell';
import { PageFallback } from './layouts/PageFallback';

// Entry routes (login/register/OAuth callback) stay in the main bundle so the
// first screen paints without an extra request. Every other page is split into
// its own chunk and loaded on first visit, so heavy dependencies (e.g. the
// charting library on the dashboard) never ship to the login page.
const DashboardPage = lazy(() =>
  import('./pages/DashboardPage').then((m) => ({ default: m.DashboardPage })),
);
const AssistantPage = lazy(() =>
  import('./pages/AssistantPage').then((m) => ({ default: m.AssistantPage })),
);
const NotesPage = lazy(() => import('./pages/NotesPage').then((m) => ({ default: m.NotesPage })));
const CalendarPage = lazy(() =>
  import('./pages/CalendarPage').then((m) => ({ default: m.CalendarPage })),
);
const StudyPlannerPage = lazy(() =>
  import('./pages/StudyPlannerPage').then((m) => ({ default: m.StudyPlannerPage })),
);
const DsaTrackerPage = lazy(() =>
  import('./pages/DsaTrackerPage').then((m) => ({ default: m.DsaTrackerPage })),
);
const ResumesPage = lazy(() =>
  import('./pages/ResumesPage').then((m) => ({ default: m.ResumesPage })),
);
const ResumeDetailPage = lazy(() =>
  import('./pages/ResumeDetailPage').then((m) => ({ default: m.ResumeDetailPage })),
);
const CertificatesPage = lazy(() =>
  import('./pages/CertificatesPage').then((m) => ({ default: m.CertificatesPage })),
);
const ProfilePage = lazy(() =>
  import('./pages/ProfilePage').then((m) => ({ default: m.ProfilePage })),
);
const SettingsPage = lazy(() =>
  import('./pages/SettingsPage').then((m) => ({ default: m.SettingsPage })),
);
const JobTrackerPage = lazy(() =>
  import('./pages/JobTrackerPage').then((m) => ({ default: m.JobTrackerPage })),
);
const ProjectsPage = lazy(() =>
  import('./pages/ProjectsPage').then((m) => ({ default: m.ProjectsPage })),
);
const PortfolioPage = lazy(() =>
  import('./pages/PortfolioPage').then((m) => ({ default: m.PortfolioPage })),
);
const BillingPage = lazy(() =>
  import('./pages/BillingPage').then((m) => ({ default: m.BillingPage })),
);
const PricingPage = lazy(() =>
  import('./pages/PricingPage').then((m) => ({ default: m.PricingPage })),
);
const TermsPage = lazy(() =>
  import('./pages/TermsPage').then((m) => ({ default: m.TermsPage })),
);
const PrivacyPage = lazy(() =>
  import('./pages/PrivacyPage').then((m) => ({ default: m.PrivacyPage })),
);
const RefundPage = lazy(() =>
  import('./pages/RefundPage').then((m) => ({ default: m.RefundPage })),
);
const ContactPage = lazy(() =>
  import('./pages/ContactPage').then((m) => ({ default: m.ContactPage })),
);
const ForgotPasswordPage = lazy(() =>
  import('./pages/ForgotPasswordPage').then((m) => ({ default: m.ForgotPasswordPage })),
);
const ResetPasswordPage = lazy(() =>
  import('./pages/ResetPasswordPage').then((m) => ({ default: m.ResetPasswordPage })),
);
const VerifyEmailPage = lazy(() =>
  import('./pages/VerifyEmailPage').then((m) => ({ default: m.VerifyEmailPage })),
);
const PublicPortfolioPage = lazy(() =>
  import('./pages/PublicPortfolioPage').then((m) => ({ default: m.PublicPortfolioPage })),
);

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth-callback" element={<OAuthCallbackPage />} />
        <Route
          path="/forgot-password"
          element={
            <Suspense fallback={<PageFallback />}>
              <ForgotPasswordPage />
            </Suspense>
          }
        />
        <Route
          path="/reset-password"
          element={
            <Suspense fallback={<PageFallback />}>
              <ResetPasswordPage />
            </Suspense>
          }
        />
        <Route
          path="/verify-email"
          element={
            <Suspense fallback={<PageFallback />}>
              <VerifyEmailPage />
            </Suspense>
          }
        />
        <Route
          path="/pricing"
          element={
            <Suspense fallback={<PageFallback />}>
              <PricingPage />
            </Suspense>
          }
        />
        <Route
          path="/terms"
          element={
            <Suspense fallback={<PageFallback />}>
              <TermsPage />
            </Suspense>
          }
        />
        <Route
          path="/privacy"
          element={
            <Suspense fallback={<PageFallback />}>
              <PrivacyPage />
            </Suspense>
          }
        />
        <Route
          path="/refunds"
          element={
            <Suspense fallback={<PageFallback />}>
              <RefundPage />
            </Suspense>
          }
        />
        <Route
          path="/contact"
          element={
            <Suspense fallback={<PageFallback />}>
              <ContactPage />
            </Suspense>
          }
        />
        <Route
          path="/u/:slug"
          element={
            <Suspense fallback={<PageFallback />}>
              <PublicPortfolioPage />
            </Suspense>
          }
        />

        <Route element={<RequireAuth />}>
          {/* AppShell wraps its <Outlet /> in Suspense, so the nav stays put while a page loads. */}
          <Route element={<AppShell />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/assistant" element={<AssistantPage />} />
            <Route path="/notes" element={<NotesPage />} />
            <Route path="/calendar" element={<CalendarPage />} />
            <Route path="/study-planner" element={<StudyPlannerPage />} />
            <Route path="/dsa" element={<DsaTrackerPage />} />
            <Route path="/resumes" element={<ResumesPage />} />
            <Route path="/resumes/:id" element={<ResumeDetailPage />} />
            <Route path="/certificates" element={<CertificatesPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/job-tracker" element={<JobTrackerPage />} />
            <Route path="/projects" element={<ProjectsPage />} />
            <Route path="/portfolio" element={<PortfolioPage />} />
            <Route path="/billing" element={<BillingPage />} />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

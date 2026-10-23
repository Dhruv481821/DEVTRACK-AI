import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OAuthCallbackPage } from './pages/OAuthCallbackPage';
import { DashboardPage } from './pages/DashboardPage';
import { ProfilePage } from './pages/ProfilePage';
import { SettingsPage } from './pages/SettingsPage';
import { ResumesPage } from './pages/ResumesPage';
import { ResumeDetailPage } from './pages/ResumeDetailPage';
import { CertificatesPage } from './pages/CertificatesPage';
import { RequireAuth } from './layouts/RequireAuth';
import { AppShell } from './layouts/AppShell';
import { JobTrackerPage } from './pages/JobTrackerPage';
import { ProjectsPage } from './pages/ProjectsPage';
import { PortfolioPage } from './pages/PortfolioPage';
import { PublicPortfolioPage } from './pages/PublicPortfolioPage';
import { NotesPage } from './pages/NotesPage';
import { CalendarPage } from './pages/CalendarPage';
import { StudyPlannerPage } from './pages/StudyPlannerPage';
import { DsaTrackerPage } from './pages/DsaTrackerPage';
import { AssistantPage } from './pages/AssistantPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth-callback" element={<OAuthCallbackPage />} />
        <Route path="/u/:slug" element={<PublicPortfolioPage />} />

        <Route element={<RequireAuth />}>
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
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
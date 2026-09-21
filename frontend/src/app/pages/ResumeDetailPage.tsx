import { Link, useParams } from 'react-router-dom';
import { ResumeDetail } from '@/features/resume/components/ResumeDetail';

export function ResumeDetailPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <p className="font-body text-sm text-danger">No resume selected.</p>;
  }

  return (
    <div>
      <Link to="/resumes" className="mb-4 inline-block font-body text-sm text-text-muted hover:text-text-primary">
        ← Back to resumes
      </Link>
      <h1 className="mb-6 font-display text-2xl">Edit resume</h1>
      <ResumeDetail resumeId={id} />
    </div>
  );
}
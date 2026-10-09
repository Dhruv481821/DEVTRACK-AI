import { JobApplicationBoard } from '@/features/jobtracker/components/JobApplicationBoard';

export function JobTrackerPage() {
  return (
    <div>
      <h1 className="mb-6 font-display text-2xl">Job Tracker</h1>
      <JobApplicationBoard />
    </div>
  );
}
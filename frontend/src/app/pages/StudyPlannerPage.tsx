import { Card } from '@/shared/ui/Card';
import { CreateStudyPlanForm } from '@/features/studyplanner/components/CreateStudyPlanForm';
import { StreakSummary } from '@/features/studyplanner/components/StreakSummary';
import { StudyPlanList } from '@/features/studyplanner/components/StudyPlanList';

export function StudyPlannerPage() {
  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl">Study Planner</h1>
      <StreakSummary />
      <Card>
        <CreateStudyPlanForm />
      </Card>
      <StudyPlanList />
    </div>
  );
}

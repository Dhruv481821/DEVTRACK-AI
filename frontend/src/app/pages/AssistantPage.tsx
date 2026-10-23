import { CareerAssistantPanel } from '@/features/ai/components/CareerAssistantPanel';
import { useAiQuota } from '@/features/billing/api/useBilling';

export function AssistantPage() {
  const { data: quota } = useAiQuota();

  return (
    <div>
      <h1 className="mb-2 font-display text-2xl">AI Assistant</h1>
      <p className="mb-6 font-body text-sm text-text-muted">
        Answers from your own tracked data across DevTrack.
        {quota && ` Your plan includes ${quota.dailyLimit} AI requests a day, shared with resume scoring.`}
      </p>
      <CareerAssistantPanel />
    </div>
  );
}

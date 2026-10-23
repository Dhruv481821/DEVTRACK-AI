import { CareerAssistantPanel } from '@/features/ai/components/CareerAssistantPanel';

export function AssistantPage() {
  return (
    <div>
      <h1 className="mb-2 font-display text-2xl">AI Assistant</h1>
      <p className="mb-6 font-body text-sm text-text-muted">
        Answers from your own tracked data across DevTrack. AI requests share a daily limit.
      </p>
      <CareerAssistantPanel />
    </div>
  );
}

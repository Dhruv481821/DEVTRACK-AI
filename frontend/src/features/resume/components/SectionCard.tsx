import { useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { SectionForm } from './SectionForm';
import { useUpdateSection, type ResumeSectionResponse } from '../api/useResumeSections';
import { SECTION_TYPE_LABELS } from '../schemas/sectionSchemas';

export function SectionCard({ resumeId, section }: { resumeId: string; section: ResumeSectionResponse }) {
  const [isEditing, setIsEditing] = useState(false);
  const updateMutation = useUpdateSection(resumeId, section.id);

  if (isEditing) {
    return (
      <Card>
        <SectionForm
          sectionType={section.sectionType}
          initialContent={section.content}
          submitting={updateMutation.isPending}
          onCancel={() => setIsEditing(false)}
          onSubmit={(content) => updateMutation.mutate({ content }, { onSuccess: () => setIsEditing(false) })}
        />
      </Card>
    );
  }

  return (
    <Card>
      <div className="mb-3 flex items-center justify-between">
        <p className="font-body text-xs font-medium uppercase tracking-wide text-text-muted">
          {SECTION_TYPE_LABELS[section.sectionType]}
        </p>
        <button
          type="button"
          onClick={() => setIsEditing(true)}
          className="font-body text-sm text-signal hover:underline"
        >
          Edit
        </button>
      </div>
      <SectionContentPreview content={section.content} />
    </Card>
  );
}

// A plain, honest key/value readout rather than a polished per-type preview layout —
// a real resume-shaped preview/PDF render is FR-RESUME-02's job, not this v1 editor.
function SectionContentPreview({ content }: { content: Record<string, unknown> }) {
  const entries = Object.entries(content).filter(([, value]) => value !== null && value !== '');

  if (entries.length === 0) {
    return <p className="font-body text-sm text-text-muted">No details yet.</p>;
  }

  return (
    <dl className="space-y-1">
      {entries.map(([key, value]) => (
        <div key={key} className="flex gap-2 font-body text-sm">
          <dt className="w-28 shrink-0 capitalize text-text-muted">{key}</dt>
          <dd className="text-text-primary">
            {Array.isArray(value) ? (
              <ul className="list-inside list-disc">
                {value.map((item, i) => (
                  <li key={i}>{String(item)}</li>
                ))}
              </ul>
            ) : (
              String(value)
            )}
          </dd>
        </div>
      ))}
    </dl>
  );
}
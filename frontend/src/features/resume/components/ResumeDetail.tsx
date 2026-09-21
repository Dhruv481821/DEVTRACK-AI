import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { useDeleteResume, useResume, useUpdateResume } from '../api/useResumes';
import { useCreateSection, useResumeSections } from '../api/useResumeSections';
import { AtsScoreCard } from './AtsScoreCard';
import { SectionCard } from './SectionCard';
import { SectionForm } from './SectionForm';
import { SECTION_TYPES, SECTION_TYPE_LABELS, type SectionType } from '../schemas/sectionSchemas';
import { updateResumeSchema, type UpdateResumeFormValues } from '../schemas/resumeSchemas';

export function ResumeDetail({ resumeId }: { resumeId: string }) {
  const navigate = useNavigate();
  const { data: resume, isLoading: isLoadingResume } = useResume(resumeId);
  const { data: sections, isLoading: isLoadingSections } = useResumeSections(resumeId);
  const updateResumeMutation = useUpdateResume(resumeId);
  const deleteResumeMutation = useDeleteResume();
  const createSectionMutation = useCreateSection(resumeId);

  const [confirmingDelete, setConfirmingDelete] = useState(false);
  const [addingType, setAddingType] = useState<SectionType | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<UpdateResumeFormValues>({ resolver: zodResolver(updateResumeSchema) });

  // Same pattern as ProfileForm.tsx — populate once the real title arrives.
  useEffect(() => {
    if (resume) {
      reset({ title: resume.title });
    }
  }, [resume, reset]);

  if (isLoadingResume) {
    return <p className="font-body text-sm text-text-muted">Loading resume…</p>;
  }

  if (!resume) {
    return <p className="font-body text-sm text-danger">Resume not found.</p>;
  }

  return (
    <div className="space-y-6">
      <Card>
        <form
          onSubmit={handleSubmit((values) => updateResumeMutation.mutate(values))}
          className="flex items-end gap-3"
        >
          <div className="flex-1">
            <Label htmlFor="resume-title">Title</Label>
            <Input id="resume-title" {...register('title')} />
            <FieldError message={errors.title?.message} />
          </div>
          <Button type="submit" loading={updateResumeMutation.isPending} disabled={!isDirty}>
            Save
          </Button>
        </form>

        <div className="mt-4 border-t border-border pt-4">
          {confirmingDelete ? (
            <div className="flex items-center gap-2">
              <span className="font-body text-sm text-text-muted">Delete this entire resume?</span>
              <Button
                type="button"
                className="bg-danger"
                loading={deleteResumeMutation.isPending}
                onClick={() => deleteResumeMutation.mutate(resumeId, { onSuccess: () => navigate('/resumes') })}
              >
                Confirm delete
              </Button>
              <Button
                type="button"
                className="bg-surface-raised text-text-muted"
                onClick={() => setConfirmingDelete(false)}
              >
                Cancel
              </Button>
            </div>
          ) : (
            <button
              type="button"
              onClick={() => setConfirmingDelete(true)}
              className="font-body text-sm text-text-muted hover:text-danger"
            >
              Delete resume
            </button>
          )}
        </div>
      </Card>

      <div>
        <h2 className="mb-3 font-display text-lg">Sections</h2>

        {isLoadingSections && <p className="font-body text-sm text-text-muted">Loading sections…</p>}

        <div className="space-y-3">
          {sections?.map((section) => (
            <SectionCard key={section.id} resumeId={resumeId} section={section} />
          ))}
        </div>

        <Card className="mt-3">
          {addingType ? (
            <SectionForm
              sectionType={addingType}
              submitting={createSectionMutation.isPending}
              onCancel={() => setAddingType(null)}
              onSubmit={(content) =>
                createSectionMutation.mutate(
                  { sectionType: addingType, content, orderIndex: sections?.length ?? 0 },
                  { onSuccess: () => setAddingType(null) },
                )
              }
            />
          ) : (
            <div>
              <p className="mb-3 font-body text-sm text-text-muted">Add a section:</p>
              <div className="flex flex-wrap gap-2">
                {SECTION_TYPES.map((type) => (
                  // sectionType is immutable after creation (backend-enforced), but a
                  // resume can still have more than one section of the same type
                  // (e.g. two Experience entries), so these stay enabled either way.
                  <Button
                    key={type}
                    type="button"
                    className="bg-surface-raised text-text-primary"
                    onClick={() => setAddingType(type)}
                  >
                    + {SECTION_TYPE_LABELS[type]}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </Card>
      </div>

      <AtsScoreCard resumeId={resumeId} />
    </div>
  );
}
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/shared/ui/Button';
import { Input, Label, FieldError } from '@/shared/ui/Input';
import { Textarea } from '@/shared/ui/Textarea';
import { docToText, textToDoc } from '../lib/noteContent';
import { useCreateNote, useUpdateNote, type NoteResponse } from '../api/useNotes';
import { noteFormSchema, parseTags, type NoteFormValues } from '../schemas/noteSchemas';

interface NoteFormProps {
  // Present = edit mode; absent = create mode.
  note?: NoteResponse;
  onDone?: () => void;
}

export function NoteForm({ note, onDone }: NoteFormProps) {
  const createMutation = useCreateNote();
  const updateMutation = useUpdateNote(note?.id ?? '');
  const mutation = note ? updateMutation : createMutation;
  const idPrefix = note ? `note-${note.id}` : 'new-note';

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<NoteFormValues>({
    resolver: zodResolver(noteFormSchema),
    defaultValues: {
      title: note?.title ?? '',
      body: note ? docToText(note.content) : '',
      tags: note?.tags.join(', ') ?? '',
    },
  });

  const onSubmit = handleSubmit((values) => {
    mutation.mutate(
      {
        title: values.title,
        content: textToDoc(values.body),
        tagNames: parseTags(values.tags),
      },
      {
        onSuccess: () => {
          if (!note) reset();
          onDone?.();
        },
      },
    );
  });

  return (
    <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4">
      <div>
        <Label htmlFor={`${idPrefix}-title`}>Title</Label>
        <Input id={`${idPrefix}-title`} {...register('title')} />
        <FieldError message={errors.title?.message} />
      </div>

      <div>
        <Label htmlFor={`${idPrefix}-body`}>Note</Label>
        <Textarea id={`${idPrefix}-body`} rows={note ? 8 : 4} {...register('body')} />
      </div>

      <div>
        <Label htmlFor={`${idPrefix}-tags`}>Tags</Label>
        <Input
          id={`${idPrefix}-tags`}
          placeholder="comma, separated"
          {...register('tags')}
        />
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <Button type="submit" loading={mutation.isPending}>
          {note ? 'Save' : 'Add note'}
        </Button>
        {note && onDone && (
          <Button
            type="button"
            className="bg-surface-raised text-text-muted"
            onClick={onDone}
          >
            Cancel
          </Button>
        )}
      </div>

      {mutation.isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t save that note. Please try again.
        </p>
      )}
    </form>
  );
}

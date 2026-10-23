import { useMemo, useState } from 'react';
import { Card } from '@/shared/ui/Card';
import { Button } from '@/shared/ui/Button';
import { Input, Label } from '@/shared/ui/Input';
import { docToText } from '../lib/noteContent';
import { useDeleteNote, useNotes } from '../api/useNotes';
import { NoteForm } from './NoteForm';

export function NoteList() {
  const { data: notes, isLoading, isError } = useNotes();
  const deleteMutation = useDeleteNote();

  const [query, setQuery] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!notes || !q) return notes;
    return notes.filter(
      (n) =>
        n.title.toLowerCase().includes(q) ||
        n.tags.some((t) => t.includes(q)) ||
        docToText(n.content).toLowerCase().includes(q),
    );
  }, [notes, query]);

  return (
    <div className="space-y-6">
      <Card>
        <NoteForm />
      </Card>

      {notes && notes.length > 0 && (
        <div>
          <Label htmlFor="notes-search">Search</Label>
          <Input
            id="notes-search"
            type="search"
            placeholder="Title, tag or text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
      )}

      {isLoading && <p className="font-body text-sm text-text-muted">Loading notes…</p>}

      {isError && (
        <p className="font-body text-sm text-danger">
          Couldn&apos;t load your notes. Please try again.
        </p>
      )}

      {!isLoading && !isError && notes?.length === 0 && (
        <p className="font-body text-sm text-text-muted">
          No notes yet — write one above.
        </p>
      )}

      {notes && notes.length > 0 && filtered?.length === 0 && (
        <p className="font-body text-sm text-text-muted">No notes match “{query}”.</p>
      )}

      <div className="space-y-3">
        {filtered?.map((note) =>
          editingId === note.id ? (
            <Card key={note.id}>
              <NoteForm note={note} onDone={() => setEditingId(null)} />
            </Card>
          ) : (
            <Card key={note.id} className="space-y-2">
              <div className="flex items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="font-body text-sm font-medium text-text-primary">
                    {note.title}
                  </p>
                  <p className="font-body text-xs text-text-muted">
                    Updated {new Date(note.updatedAt).toLocaleDateString()}
                  </p>
                </div>

                {confirmingId === note.id ? (
                  <div className="flex shrink-0 items-center gap-2">
                    <span className="font-body text-sm text-text-muted">Delete?</span>
                    <Button
                      type="button"
                      className="bg-danger"
                      loading={deleteMutation.isPending}
                      onClick={() =>
                        deleteMutation.mutate(note.id, {
                          onSettled: () => setConfirmingId(null),
                        })
                      }
                    >
                      Confirm
                    </Button>
                    <Button
                      type="button"
                      className="bg-surface-raised text-text-muted"
                      onClick={() => setConfirmingId(null)}
                    >
                      Cancel
                    </Button>
                  </div>
                ) : (
                  <div className="flex shrink-0 items-center gap-3">
                    <button
                      type="button"
                      onClick={() => setEditingId(note.id)}
                      className="font-body text-sm text-text-muted hover:text-text-primary"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmingId(note.id)}
                      className="font-body text-sm text-text-muted hover:text-danger"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>

              {docToText(note.content) && (
                <p className="line-clamp-4 whitespace-pre-line font-body text-sm text-text-muted">
                  {docToText(note.content)}
                </p>
              )}

              {note.tags.length > 0 && (
                <ul className="flex flex-wrap gap-2">
                  {note.tags.map((tag) => (
                    <li
                      key={tag}
                      className="rounded-md border border-border px-2 py-0.5 font-mono text-xs text-text-muted"
                    >
                      {tag}
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          ),
        )}
      </div>
    </div>
  );
}

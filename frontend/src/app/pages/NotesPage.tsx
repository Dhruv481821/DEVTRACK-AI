import { NoteList } from '@/features/notes/components/NoteList';

export function NotesPage() {
  return (
    <div>
      <h1 className="mb-6 font-display text-2xl">Notes</h1>
      <NoteList />
    </div>
  );
}

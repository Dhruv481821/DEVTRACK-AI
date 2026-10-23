// The backend stores note content as a Tiptap-shaped JSON document
// (CreateNoteRequest.content). The UI edits plain text for now, so these two
// helpers convert between the two. Output is a valid Tiptap doc, so a rich
// editor can be dropped in later without migrating stored notes.

export interface TiptapNode {
  type?: string;
  text?: string;
  content?: TiptapNode[];
  [key: string]: unknown;
}

export function textToDoc(text: string): TiptapNode {
  const paragraphs = text.split('\n');
  return {
    type: 'doc',
    content: paragraphs.map((line) =>
      line.length > 0
        ? { type: 'paragraph', content: [{ type: 'text', text: line }] }
        : { type: 'paragraph' },
    ),
  };
}

function nodeText(node: TiptapNode): string {
  if (typeof node.text === 'string') return node.text;
  return (node.content ?? []).map(nodeText).join('');
}

// Block-level children of the doc become lines; unknown shapes degrade to ''.
export function docToText(doc: unknown): string {
  if (!doc || typeof doc !== 'object') return '';
  const root = doc as TiptapNode;
  if (!Array.isArray(root.content)) return nodeText(root);
  return root.content.map(nodeText).join('\n');
}

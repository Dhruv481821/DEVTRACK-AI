import { describe, it, expect } from 'vitest';
import { docToText, textToDoc } from '../noteContent';

describe('noteContent', () => {
  it('round-trips multi-line text including blank lines', () => {
    const text = 'First line\n\nThird line';
    expect(docToText(textToDoc(text))).toBe(text);
  });

  it('produces a Tiptap doc with paragraph nodes', () => {
    expect(textToDoc('hi')).toEqual({
      type: 'doc',
      content: [{ type: 'paragraph', content: [{ type: 'text', text: 'hi' }] }],
    });
  });

  it('flattens nested inline nodes from a richer document', () => {
    const doc = {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: [
            { type: 'text', text: 'bold ' },
            { type: 'text', text: 'and plain', marks: [{ type: 'bold' }] },
          ],
        },
      ],
    };
    expect(docToText(doc)).toBe('bold and plain');
  });

  it('returns empty string for null or malformed content', () => {
    expect(docToText(null)).toBe('');
    expect(docToText('not a doc')).toBe('');
  });
});

/** BlogEditor – manage blog posts listed on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'title', label: 'Post Title' },
  { key: 'author', label: 'Author' },
  { key: 'date', label: 'Date (e.g. 2025-01-15)' },
  { key: 'summary', label: 'Summary / Excerpt', multiline: true },
  { key: 'link', label: 'Full Article URL (optional)' },
];

const EMPTY = { title: '', author: '', date: '', summary: '', link: '' };

export default function BlogEditor() {
  return <JsonListEditor contentKey="json.blog" fields={FIELDS} emptyItem={EMPTY} title="Blog Post" />;
}

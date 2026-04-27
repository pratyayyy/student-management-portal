/** TestimonialsEditor – manage student testimonials on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'name', label: 'Student Name' },
  { key: 'batch', label: 'Batch / Year' },
  { key: 'quote', label: 'Testimonial Text', multiline: true },
  { key: 'rating', label: 'Rating (1–5)' },
];

const EMPTY = { name: '', batch: '', quote: '', rating: '5' };

export default function TestimonialsEditor() {
  return <JsonListEditor contentKey="json.testimonials" fields={FIELDS} emptyItem={EMPTY} title="Testimonial" />;
}

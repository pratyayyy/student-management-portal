/** CoursesEditor – manage the list of courses shown on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'name', label: 'Course Name' },
  { key: 'description', label: 'Description', multiline: true },
  { key: 'duration', label: 'Duration (e.g. 6 months)' },
  { key: 'fees', label: 'Fees (e.g. ₹12,000)' },
  { key: 'eligibility', label: 'Eligibility' },
];

const EMPTY = { name: '', description: '', duration: '', fees: '', eligibility: '' };

export default function CoursesEditor() {
  return <JsonListEditor contentKey="json.courses" fields={FIELDS} emptyItem={EMPTY} title="Course" />;
}

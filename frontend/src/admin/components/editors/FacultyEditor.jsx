/** FacultyEditor – manage faculty profiles shown on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'name', label: 'Full Name' },
  { key: 'designation', label: 'Designation / Title' },
  { key: 'qualification', label: 'Qualifications' },
  { key: 'experience', label: 'Years of Experience (e.g. 10)' },
  { key: 'bio', label: 'Short Bio', multiline: true },
];

const EMPTY = { name: '', designation: '', qualification: '', experience: '', bio: '' };

export default function FacultyEditor() {
  return <JsonListEditor contentKey="json.faculty" fields={FIELDS} emptyItem={EMPTY} title="Faculty Member" />;
}

/** ResultsEditor – manage student results / achievements on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'studentName', label: 'Student Name' },
  { key: 'exam', label: 'Exam / Achievement' },
  { key: 'score', label: 'Score / Rank' },
  { key: 'year', label: 'Year' },
  { key: 'description', label: 'Additional Details', multiline: true },
];

const EMPTY = { studentName: '', exam: '', score: '', year: '', description: '' };

export default function ResultsEditor() {
  return <JsonListEditor contentKey="json.results" fields={FIELDS} emptyItem={EMPTY} title="Result" />;
}

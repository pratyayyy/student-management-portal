/** FAQEditor – manage Frequently Asked Questions on the promo site. */
import JsonListEditor from './JsonListEditor';

const FIELDS = [
  { key: 'question', label: 'Question' },
  { key: 'answer', label: 'Answer', multiline: true },
];

const EMPTY = { question: '', answer: '' };

export default function FAQEditor() {
  return <JsonListEditor contentKey="json.faq" fields={FIELDS} emptyItem={EMPTY} title="FAQ Item" />;
}

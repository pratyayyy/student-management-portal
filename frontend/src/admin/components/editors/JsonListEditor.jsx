/**
 * JsonListEditor – reusable editor for JSON array content blocks.
 * Used by CoursesEditor, FacultyEditor, etc.
 *
 * Props:
 *  - contentKey: the SiteContent key (e.g. "json.courses")
 *  - fields: array of { key, label, multiline? } defining each item's fields
 *  - emptyItem: object with default values for a new list item
 *  - title: display label (e.g. "Course")
 */
import { useState } from 'react';
import useAdminConfigStore from '../../../store/adminConfigStore';

export default function JsonListEditor({ contentKey, fields, emptyItem, title }) {
  const { contentConfig, setContentSection, saving } = useAdminConfigStore();

  // Parse stored JSON; default to empty array on parse error.
  const parseItems = () => {
    try {
      return JSON.parse(contentConfig[contentKey] || '[]');
    } catch {
      return [];
    }
  };

  const [items, setItems] = useState(parseItems);
  const [success, setSuccess] = useState(false);

  const handleChange = (index, field, value) => {
    setItems((prev) => prev.map((item, i) => i === index ? { ...item, [field]: value } : item));
  };

  const handleAdd = () => setItems((prev) => [...prev, { ...emptyItem, id: crypto.randomUUID() }]);

  const handleDelete = (index) => setItems((prev) => prev.filter((_, i) => i !== index));

  const handleSave = async () => {
    await setContentSection({ [contentKey]: JSON.stringify(items) });
    setSuccess(true);
    setTimeout(() => setSuccess(false), 3000);
  };

  return (
    <div className="space-y-4">
      {items.length === 0 && (
        <div className="text-center py-8 text-slate-400">
          <p className="text-3xl mb-2">📭</p>
          <p className="text-sm">No {title.toLowerCase()}s yet. Click "Add" to create one.</p>
        </div>
      )}

      {items.map((item, index) => (
        <div key={item.id || index} className="card p-4 space-y-3 border border-slate-200">
          <div className="flex items-center justify-between">
            <span className="text-sm font-semibold text-slate-700">{title} #{index + 1}</span>
            <button
              onClick={() => handleDelete(index)}
              className="text-xs px-3 py-1 bg-red-50 text-red-600 border border-red-200 rounded-lg hover:bg-red-100 transition"
            >
              Delete
            </button>
          </div>
          {fields.map(({ key, label, multiline }) => (
            <div key={key}>
              <label className="block text-xs font-medium text-slate-600 mb-1">{label}</label>
              {multiline ? (
                <textarea
                  rows={2}
                  className="input w-full resize-y text-sm"
                  value={item[key] || ''}
                  onChange={(e) => handleChange(index, key, e.target.value)}
                />
              ) : (
                <input
                  className="input w-full text-sm"
                  value={item[key] || ''}
                  onChange={(e) => handleChange(index, key, e.target.value)}
                />
              )}
            </div>
          ))}
        </div>
      ))}

      <div className="flex items-center gap-3">
        <button
          onClick={handleAdd}
          className="px-4 py-2 text-sm font-medium bg-indigo-50 text-indigo-700 border border-indigo-200 rounded-lg hover:bg-indigo-100 transition"
        >
          ➕ Add {title}
        </button>
        <button onClick={handleSave} disabled={saving} className="btn-primary px-5 py-2 text-sm">
          {saving ? 'Saving…' : 'Save All'}
        </button>
        {success && <span className="text-sm text-emerald-600 font-medium">✅ Saved!</span>}
      </div>
    </div>
  );
}

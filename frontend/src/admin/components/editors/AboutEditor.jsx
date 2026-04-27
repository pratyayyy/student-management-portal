/** AboutEditor – edit the About section text and mission statement. */
import { useState } from 'react';
import useAdminConfigStore from '../../../store/adminConfigStore';

export default function AboutEditor() {
  const { contentConfig, setContentSection, saving } = useAdminConfigStore();
  const [local, setLocal] = useState({
    'about.heading': contentConfig['about.heading'] || '',
    'about.para1': contentConfig['about.para1'] || '',
    'about.para2': contentConfig['about.para2'] || '',
    'about.mission': contentConfig['about.mission'] || '',
  });
  const [success, setSuccess] = useState(false);

  const handleChange = (key, value) => setLocal((prev) => ({ ...prev, [key]: value }));

  const handleSave = async () => {
    await setContentSection(local);
    setSuccess(true);
    setTimeout(() => setSuccess(false), 3000);
  };

  const fields = [
    { key: 'about.heading', label: 'Section Heading', multiline: false },
    { key: 'about.para1', label: 'Paragraph 1', multiline: true },
    { key: 'about.para2', label: 'Paragraph 2', multiline: true },
    { key: 'about.mission', label: 'Mission Statement', multiline: true },
  ];

  return (
    <div className="space-y-4">
      {fields.map(({ key, label, multiline }) => (
        <div key={key}>
          <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
          {multiline ? (
            <textarea
              rows={3}
              className="input w-full resize-y"
              value={local[key]}
              onChange={(e) => handleChange(key, e.target.value)}
            />
          ) : (
            <input
              className="input w-full"
              value={local[key]}
              onChange={(e) => handleChange(key, e.target.value)}
            />
          )}
        </div>
      ))}
      <div className="flex items-center gap-3">
        <button onClick={handleSave} disabled={saving} className="btn-primary px-5 py-2 text-sm">
          {saving ? 'Saving…' : 'Save'}
        </button>
        {success && <span className="text-sm text-emerald-600 font-medium">✅ Saved!</span>}
      </div>
    </div>
  );
}
